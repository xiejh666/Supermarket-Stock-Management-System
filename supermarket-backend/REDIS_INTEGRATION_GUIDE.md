# Redis 集成使用指南

本文档说明如何在超市管理系统中使用 Redis 的各项功能。

## 一、已实现的 Redis 功能

### 1. Token 黑名单机制
**用途**：实现强制登出、踢人下线功能

**使用场景**：
- 用户修改密码后，旧 Token 失效
- 管理员禁用用户账号
- 用户主动登出

**使用示例**：

```java
@Autowired
private TokenBlacklistService tokenBlacklistService;

// 1. 用户登出时，将 Token 加入黑名单
public void logout(String token) {
    // 计算 Token 剩余有效时间（秒）
    long expireSeconds = getTokenExpireSeconds(token);
    tokenBlacklistService.addToBlacklist(token, expireSeconds);
}

// 2. 在 JWT 过滤器中检查 Token 是否在黑名单
public boolean validateToken(String token) {
    if (tokenBlacklistService.isBlacklisted(token)) {
        return false; // Token 已失效
    }
    // ... 其他验证逻辑
    return true;
}

// 3. 禁用用户时，将该用户所有 Token 加入黑名单
public void disableUser(Long userId) {
    tokenBlacklistService.blacklistUserTokens(userId);
    // ... 更新数据库用户状态
}
```

### 2. 登录限流防暴力破解
**用途**：防止恶意用户暴力破解密码

**配置**：
- 最大失败次数：5次
- 锁定时间：30分钟

**使用示例**：

```java
@Autowired
private LoginLimitService loginLimitService;

public Result login(String username, String password) {
    // 1. 检查是否被锁定
    if (loginLimitService.isLocked(username)) {
        long remainingTime = loginLimitService.getRemainingLockTime(username);
        return Result.error("账号已被锁定，请" + (remainingTime / 60) + "分钟后再试");
    }
    
    // 2. 验证用户名密码
    User user = userService.getByUsername(username);
    if (user == null || !passwordMatches(password, user.getPassword())) {
        // 记录失败次数
        int failCount = loginLimitService.recordLoginFailure(username);
        if (failCount >= 5) {
            return Result.error("登录失败次数过多，账号已被锁定30分钟");
        }
        return Result.error("用户名或密码错误，还可尝试" + (5 - failCount) + "次");
    }
    
    // 3. 登录成功，清除失败记录
    loginLimitService.clearLoginFailure(username);
    
    // 生成 Token 并返回
    String token = generateToken(user);
    return Result.success(token);
}
```

### 3. 热点数据缓存
**用途**：缓存高频查询的数据，减轻数据库压力

**使用示例**：

```java
@Autowired
private CacheService cacheService;

// 1. 缓存商品列表（缓存10分钟）
public List<Product> getProductList() {
    String cacheKey = "product:list";
    return cacheService.getOrLoad(cacheKey, () -> {
        // 从数据库查询
        return productMapper.selectList(null);
    }, 10);
}

// 2. 缓存分类树（缓存30分钟）
public List<Category> getCategoryTree() {
    String cacheKey = "category:tree";
    return cacheService.getOrLoad(cacheKey, () -> {
        // 从数据库查询并构建树形结构
        return buildCategoryTree();
    }, 30);
}

// 3. 更新商品后，删除缓存
public void updateProduct(Product product) {
    productMapper.updateById(product);
    // 删除商品列表缓存
    cacheService.evict("product:list");
    // 删除单个商品缓存
    cacheService.evict("product:" + product.getId());
}

// 4. 批量删除缓存（使用通配符）
public void clearAllProductCache() {
    cacheService.evictByPattern("product:*");
}
```

### 4. 库存预警统计缓存
**用途**：缓存库存预警数量，避免频繁统计

**使用示例**：

```java
@Autowired
private CacheService cacheService;

// 获取库存预警数量（缓存5分钟）
public Integer getLowStockCount() {
    String cacheKey = "inventory:low_stock_count";
    return cacheService.getOrLoad(cacheKey, () -> {
        // 从数据库统计
        return inventoryMapper.countLowStock();
    }, 5);
}

// 库存变更时，刷新缓存
public void updateInventory(Long productId, Integer quantity) {
    inventoryMapper.updateStock(productId, quantity);
    // 刷新库存预警统计
    cacheService.evict("inventory:low_stock_count");
}
```

### 5. 销售统计缓存
**用途**：缓存销售统计数据，提升Dashboard性能

**使用示例**：

```java
@Autowired
private CacheService cacheService;

// 获取今日销售额（缓存1分钟）
public BigDecimal getTodaySales() {
    String cacheKey = "sales:today:" + LocalDate.now();
    return cacheService.getOrLoad(cacheKey, () -> {
        return saleMapper.sumTodaySales();
    }, 1);
}

// 获取本月销售额（缓存10分钟）
public BigDecimal getMonthSales() {
    String cacheKey = "sales:month:" + YearMonth.now();
    return cacheService.getOrLoad(cacheKey, () -> {
        return saleMapper.sumMonthSales();
    }, 10);
}

// 新增销售订单后，清除相关缓存
public void createSaleOrder(SaleOrder order) {
    saleMapper.insert(order);
    // 清除今日和本月销售额缓存
    cacheService.evict("sales:today:" + LocalDate.now());
    cacheService.evict("sales:month:" + YearMonth.now());
}
```

## 二、Redis 键命名规范

为了便于管理和维护，建议遵循以下命名规范：

```
业务模块:功能:参数

示例：
- token:blacklist:{token}          # Token黑名单
- login:fail:{username}             # 登录失败记录
- product:list                      # 商品列表
- product:{id}                      # 单个商品
- category:tree                     # 分类树
- inventory:low_stock_count         # 库存预警数量
- sales:today:{date}                # 每日销售额
- sales:month:{yearMonth}           # 每月销售额
```

## 三、注意事项

### 1. 缓存过期时间设置建议
- **高频且不常变化的数据**：30-60分钟（如分类树）
- **中频数据**：10-30分钟（如商品列表）
- **实时性要求高的数据**：1-5分钟（如销售统计）
- **Token黑名单**：与Token有效期一致

### 2. 缓存更新策略
- **主动更新**：数据变更时立即删除或更新缓存
- **被动更新**：设置过期时间，过期后自动重新加载

### 3. 缓存穿透防护
对于可能不存在的数据，也应该缓存一个空值，避免每次都查询数据库：

```java
public Product getProductById(Long id) {
    String cacheKey = "product:" + id;
    return cacheService.getOrLoad(cacheKey, () -> {
        Product product = productMapper.selectById(id);
        // 即使查询结果为null，也会被缓存
        return product;
    }, 10);
}
```

### 4. 启动 Redis 服务
在使用前，请确保 Redis 服务已启动：

**Windows**：
```bash
redis-server.exe
```

**Linux/Mac**：
```bash
redis-server
```

或使用 Docker：
```bash
docker run -d -p 6379:6379 --name redis redis:latest
```

## 四、下一步集成建议

1. **在 AuthController 中集成登录限流**
2. **在 JWT 过滤器中集成 Token 黑名单检查**
3. **在 UserService 中集成用户禁用时的 Token 黑名单**
4. **在 ProductService 中集成商品列表缓存**
5. **在 CategoryService 中集成分类树缓存**
6. **在 DashboardController 中集成销售统计缓存**

如需具体的代码修改示例，请告知具体的文件路径，我将为您提供详细的集成代码。
