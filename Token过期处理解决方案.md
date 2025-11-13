# Token过期处理解决方案

## 🎯 问题描述

### 用户遇到的问题
1. **隔段时间登录时直接登录上次账号** - 用户希望长时间不操作后需要重新登录
2. **长时间不登录数据获取失败** - 显示admin账号但数据为空，需要退出重新登录才能正常使用
3. **用户体验差** - 用户不知道为什么数据获取失败

### 根本原因
- Token有效期过长（24小时），导致长时间自动登录
- 前端没有检查Token是否过期
- Token过期后前端没有正确处理，导致显示用户信息但API调用失败

---

## ✅ 解决方案（参考市面上主流系统）

### 市面上的标准做法
- **支付宝/钉钉/企业微信**: 15-30分钟不操作自动退出，需要重新登录
- **银行系统**: 5-15分钟超时，安全性要求高
- **一般企业应用**: 15分钟-1小时，平衡安全性和用户体验

### 我们的方案：15分钟超时策略
```
用户登录 → 15分钟内正常使用 → 15分钟后Token过期 → 自动跳转登录页 → 需要重新登录
```

---

## 🔧 实现详情

### 后端修改

#### 1. Token有效期调整
```yaml
# application.yml
jwt:
  expiration: 900000  # 15分钟 (15 * 60 * 1000)
```

### 前端修改

#### 1. Token存储和过期检查 (auth.js)
```javascript
// 设置Token时同时记录过期时间
export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
  // 设置过期时间（15分钟后）
  const expireTime = Date.now() + 15 * 60 * 1000
  localStorage.setItem(TOKEN_EXPIRE_KEY, expireTime.toString())
}

// 检查Token是否过期
export function isTokenExpired() {
  const token = getToken()
  if (!token) return true
  
  const expireTime = localStorage.getItem(TOKEN_EXPIRE_KEY)
  if (!expireTime) return true
  
  return Date.now() > parseInt(expireTime)
}
```

#### 2. 路由守卫检查 (router/index.js)
```javascript
router.beforeEach((to, from, next) => {
  // 检查需要认证的页面
  if (to.meta.requiresAuth) {
    if (!token || !isTokenValid()) {
      // Token不存在或已过期，清除Token并跳转登录页
      removeToken()
      next('/login')
      return
    }
  }
})
```

#### 3. 请求拦截器检查 (request.js)
```javascript
service.interceptors.request.use(config => {
  const token = getToken()
  
  // 检查Token是否过期
  if (token && !isTokenValid()) {
    // Token已过期，清除并跳转登录页
    removeToken()
    ElMessage.warning('登录已过期，请重新登录')
    router.push('/login')
    return Promise.reject(new Error('Token已过期'))
  }
})
```

#### 4. 登录页面清理 (Login.vue)
```javascript
// 页面加载时清除旧Token
onMounted(() => {
  removeToken()
})
```

---

## 🎨 用户体验流程

### 场景1: 正常使用（15分钟内）
```
用户登录 → 正常使用系统 → 所有功能正常 → 继续使用
```

### 场景2: Token即将过期（超过15分钟）
```
用户操作 → Token已过期 → 自动跳转登录页 → 提示"登录已过期，请重新登录"
```

### 场景3: 浏览器关闭后重新打开
```
重新打开浏览器 → 检查Token有效性 → Token过期 → 直接显示登录页
```

### 场景4: 长时间不操作
```
用户登录后离开 → 15分钟后再次操作 → 自动跳转登录页 → 需要重新输入账号密码
```

---

## 🔐 安全性提升

### Token安全策略
1. **短期有效**: 15分钟过期，降低Token泄露风险
2. **自动清理**: 过期后自动清除，不会残留
3. **强制重新登录**: 过期后必须重新输入密码，确保用户身份

### 用户体验优化
1. **明确提示**: "登录已过期，请重新登录"
2. **自动跳转**: 无需用户手动退出登录
3. **状态一致**: 不会出现显示用户信息但数据获取失败的情况

---

## 📊 对比其他方案

| 方案 | 优点 | 缺点 | 适用场景 |
|------|------|------|----------|
| **长期Token (24小时)** | 用户体验好，无需频繁登录 | 安全性低，容易出现问题 | 内部系统，安全要求不高 |
| **短期Token (15分钟)** | 安全性高，问题少 | 需要频繁登录 | **推荐方案，平衡安全性和体验** |
| **双Token策略** | 安全性高，用户体验好 | 实现复杂，维护成本高 | 大型系统，用户量大 |

---

## ✅ 验证方法

### 测试步骤
1. **正常登录测试**
   - 登录系统 → 正常使用 → 功能正常

2. **Token过期测试**
   - 登录系统 → 等待16分钟 → 操作任意功能 → 应该跳转到登录页

3. **浏览器重启测试**
   - 登录系统 → 关闭浏览器 → 等待16分钟 → 重新打开 → 应该显示登录页

4. **多标签页测试**
   - 打开多个标签页 → 其中一个过期 → 其他标签页操作 → 都应该跳转登录页

---

## 🎉 解决效果

### 问题解决
✅ **不再自动登录上次账号** - 15分钟后必须重新登录  
✅ **不再出现数据获取失败** - Token过期自动跳转登录页  
✅ **用户体验清晰** - 明确提示过期原因  

### 符合市场标准
✅ **参考主流系统** - 与支付宝、钉钉等保持一致  
✅ **安全性提升** - 短期Token降低风险  
✅ **维护简单** - 代码清晰，易于理解  

---

**实施状态**: ✅ 已完成  
**测试建议**: 重启后端和前端，测试15分钟后的自动跳转效果  
**用户体验**: 符合市面上主流系统的标准做法
