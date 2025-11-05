# 超市进销存管理系统

基于SpringBoot + Vue3 + MySQL的超市进销存管理系统

## 项目结构

```
supermarket-system/
├── supermarket-backend/     # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/supermarket/
│   │   │   │       ├── SupermarketApplication.java
│   │   │   │       ├── common/          # 公共类
│   │   │   │       ├── config/          # 配置类
│   │   │   │       ├── controller/       # 控制器
│   │   │   │       ├── service/         # 服务层
│   │   │   │       ├── mapper/          # 数据访问层
│   │   │   │       ├── entity/          # 实体类
│   │   │   │       ├── dto/             # 数据传输对象
│   │   │   │       ├── vo/              # 视图对象
│   │   │   │       ├── utils/           # 工具类
│   │   │   │       └── exception/       # 异常处理
│   │   │   └── resources/
│   │   │       ├── application.yml      # 配置文件
│   │   │       └── mapper/              # MyBatis映射文件
│   │   └── test/
│   └── pom.xml
├── supermarket-frontend/    # 前端项目
│   ├── src/
│   │   ├── api/             # API接口
│   │   ├── components/      # 组件
│   │   ├── views/           # 页面
│   │   ├── router/          # 路由
│   │   ├── store/           # 状态管理
│   │   ├── utils/           # 工具类
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   └── vite.config.js
└── sql/                      # 数据库脚本
    ├── schema.sql           # 建表脚本
    └── data.sql             # 初始数据脚本
```

## 技术栈

### 后端
- Spring Boot 2.7.14
- MyBatis Plus 3.5.3.1
- Spring Security
- JWT
- MySQL 8.0
- Redis
- Knife4j (Swagger)

### 前端
- Vue 3
- Element Plus
- Pinia
- Vue Router
- Axios
- ECharts
- Vite

## 开发环境要求

- JDK 1.8+
- Maven 3.6+
- Node.js 16+
- MySQL 8.0+
- Redis 6.0+

## 快速开始

### 1. 后端启动

```bash
cd supermarket-backend
mvn clean install
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动

API文档地址：http://localhost:8080/api/doc.html

### 2. 前端启动

```bash
cd supermarket-frontend
npm install
npm run dev
```

前端服务将在 http://localhost:3000 启动

### 3. 数据库初始化

执行 `sql/schema.sql` 和 `sql/data.sql` 脚本初始化数据库

## 功能模块

### 管理员功能
- 员工管理
- 权限管理
- 商品管理
- 分类管理
- 采购审核
- 销售订单管理
- 库存管理
- 销售统计
- 库存统计
- 供应商管理

### 采购员功能
- 采购申请
- 确认入库
- 库存查询
- 库存预警

### 营业员功能
- 商品销售
- 客户管理
- 订单管理
- 订单商品管理
- 工资单

## 开发计划

### 已完成阶段

- [x] **阶段一：项目初始化与环境搭建**
  - [x] Spring Boot后端项目初始化
  - [x] Vue3前端项目初始化
  - [x] 项目结构规划
  - [x] 依赖配置（pom.xml, package.json）

- [x] **阶段二：数据库设计与实现**
  - [x] 数据库表设计（schema.sql）
  - [x] 初始化数据准备（data.sql）
  - [x] 实体类创建（Entity层）
    - [x] SysUser - 用户表
    - [x] SysRole - 角色表
    - [x] SysPermission - 权限表
    - [x] SysRolePermission - 角色权限关联表
    - [x] Category - 商品分类表
    - [x] Product - 商品表
    - [x] Supplier - 供应商表

- [x] **后端基础组件开发**
  - [x] 公共响应类（Result, ResultCode）
  - [x] 配置类（MyBatisPlusConfig, SwaggerConfig）
  - [x] 工具类（JwtUtils）
  - [x] 异常处理（BusinessException, GlobalExceptionHandler）

- [x] **前端基础框架搭建**
  - [x] 路由配置（Vue Router）
  - [x] 状态管理（Pinia - user store）
  - [x] 工具类（request.js, auth.js）
  - [x] 基础页面（Login, Layout, Dashboard, 404）

- [x] **阶段三：后端核心功能开发** ✅ **已完成**
  - [x] Controller层（控制器）
    - [x] 认证管理Controller (AuthController)
    - [x] 用户管理Controller (SysUserController)
    - [x] 角色权限Controller (SysRoleController)
    - [x] 商品管理Controller (ProductController)
    - [x] 分类管理Controller (CategoryController)
    - [x] 供应商管理Controller (SupplierController)
    - [x] 采购管理Controller (PurchaseOrderController)
    - [x] 销售管理Controller (SaleOrderController)
    - [x] 库存管理Controller (InventoryController)
    - [x] 统计分析Controller (StatisticsController)
  - [x] Service层（服务层）
    - [x] AuthService - 认证服务
    - [x] SysUserService - 用户服务
    - [x] SysRoleService - 角色服务
    - [x] CategoryService - 分类服务
    - [x] ProductService - 商品服务
    - [x] SupplierService - 供应商服务
    - [x] PurchaseOrderService - 采购服务
    - [x] SaleOrderService - 销售服务
    - [x] InventoryService - 库存服务
    - [x] StatisticsService - 统计服务
  - [x] Mapper层（数据访问层）
  - [x] DTO和VO类
  - [x] MyBatis XML映射文件
  - [x] 字段自动填充处理器

- [x] **阶段四：前端核心功能开发** ✅ **已完成100%**
  - [x] API接口封装（10个接口模块）
  - [x] 全局样式系统（渐变主题、动画效果）
  - [x] 布局系统（侧边栏、顶栏、导航）
  - [x] 首页仪表盘（数据可视化、ECharts图表）
  - [x] 商品管理页面（完整CRUD功能）
  - [x] 分类管理页面（完整CRUD功能）
  - [x] 用户管理页面（用户管理、角色管理、状态切换）
  - [x] 供应商管理页面（完整CRUD功能）
  - [x] 采购管理页面（订单管理、入库确认）
  - [x] 销售管理页面（订单管理、出库确认）
  - [x] 库存管理页面（库存查询、调整、预警）

### 🔄 当前阶段

### 未开始阶段

- [ ] **阶段五：系统集成与测试**
  - [ ] 前后端联调
  - [ ] 功能测试
  - [ ] 性能测试
  - [ ] Bug修复

- [ ] **阶段六：优化与文档**
  - [ ] 代码优化
  - [ ] 接口文档完善
  - [ ] 部署文档编写
  - [ ] 用户手册编写

---

**当前进度总结**：
- ✅ **后端开发已100%完成**：
  - 数据库设计与实体类（16个表，16个实体类）
  - 业务层完整实现（10个Controller，10个Service，11个Mapper）
  - 完整的DTO/VO体系
  - MyBatis XML映射文件
  - 统一异常处理和响应格式
  - Swagger API文档支持
  - Apifox接口测试文档
  
- ✅ **前端开发已100%完成**：
  - ✅ API接口封装（10个模块，所有后端接口已对接）
  - ✅ 全局样式系统（variables.scss、global.scss、animations.scss）
  - ✅ 现代化布局（渐变侧边栏、响应式顶栏、面包屑导航）
  - ✅ 首页仪表盘（4个统计卡片、4个ECharts图表、实时数据展示）
  - ✅ 商品管理页面（搜索、表格、CRUD、状态管理）
  - ✅ 分类管理页面（完整CRUD功能）
  - ✅ 用户管理页面（用户管理、角色管理、状态切换、密码重置）
  - ✅ 供应商管理页面（完整CRUD功能）
  - ✅ 采购管理页面（订单管理、批量添加商品、入库确认）
  - ✅ 销售管理页面（订单管理、批量添加商品、出库确认）
  - ✅ 库存管理页面（库存查询、调整、预警、统计卡片）
  
- ⏸ **未开始**：系统集成测试和部署

---

## 后端接口测试

### 使用 Apifox 测试
项目根目录下的 `Apifox接口测试指南.md` 提供了完整的接口测试文档，包括：
- 所有接口的详细说明
- 请求参数和响应示例
- 接口测试流程指导
- 环境配置建议

### 使用 Swagger 文档
启动后端服务后，访问：`http://localhost:8080/api/doc.html`

### 数据库管理
使用 DataGrip 连接数据库：
- **Host**: localhost
- **Port**: 3306
- **Database**: supermarket_db
- **Username**: root
- **Password**: （根据实际配置）

执行 `sql/schema.sql` 和 `sql/data.sql` 初始化数据库。

---

## 前端开发说明

### 前端特点 ✨
本系统前端采用**现代化设计**，具有以下特色：
- 🎨 **渐变主题** - 蓝紫色渐变配色，视觉效果出众
- ✨ **流畅动画** - 淡入、缩放、悬停等多种过渡效果
- 📊 **数据可视化** - ECharts图表，数据展示直观美观
- 🎯 **现代化UI** - Element Plus + 自定义样式
- 📱 **响应式设计** - 适配各种屏幕尺寸

### 已完成的页面
- ✅ **Layout.vue** - 主布局（侧边栏、顶栏、导航）
- ✅ **Dashboard.vue** - 首页仪表盘（数据统计、图表可视化）
- ✅ **Product.vue** - 商品管理（完整CRUD功能）

### 待开发的页面
参考 `前端开发完成总结.md` 文档，按照 `Product.vue` 的模式快速开发：
- 📝 Category.vue - 分类管理
- 📝 Supplier.vue - 供应商管理  
- 📝 Purchase.vue - 采购管理
- 📝 Sale.vue - 销售管理
- 📝 Inventory.vue - 库存管理
- 📝 User.vue - 用户管理

### 快速开发指南
1. 复制 `Product.vue` 作为模板
2. 修改API接口调用
3. 调整表格列和表单字段
4. 应用全局样式类

详细开发指导请查看 `前端开发完成总结.md`。

### 启动前端
```bash
cd supermarket-frontend
npm install
npm run dev
```

访问：http://localhost:3000

## 许可证

MIT License

