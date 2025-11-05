# Apifox 接口测试指南

## 项目概述

超市进销存管理系统后端API接口文档，用于 Apifox 接口测试。

## 基础信息

- **Base URL**: `http://localhost:8080`
- **API前缀**: `/api`
- **Content-Type**: `application/json`

## 接口分类

### 1. 认证管理 (AuthController)

#### 1.1 用户登录
- **接口**: `POST /api/auth/login`
- **请求体**:
```json
{
  "username": "admin",
  "password": "123456"
}
```
- **响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "username": "admin",
    "realName": "管理员",
    "roleCode": "ADMIN",
    "roleName": "系统管理员"
  }
}
```

#### 1.2 用户登出
- **接口**: `POST /api/auth/logout`
- **请求头**: `Authorization: Bearer {token}`

---

### 2. 用户管理 (SysUserController)

#### 2.1 分页查询用户列表
- **接口**: `GET /api/users/list`
- **请求参数**:
  - `current`: 当前页，默认1
  - `size`: 每页条数，默认10
  - `username`: 用户名（可选）
  - `realName`: 真实姓名（可选）
  - `status`: 状态（可选）

#### 2.2 创建用户
- **接口**: `POST /api/users`
- **请求体**:
```json
{
  "username": "test001",
  "password": "123456",
  "realName": "测试用户",
  "phone": "13800138000",
  "email": "test@example.com",
  "roleId": 2,
  "status": 1
}
```

#### 2.3 更新用户
- **接口**: `PUT /api/users`
- **请求体**: (与创建用户相同，但需包含id)

#### 2.4 删除用户
- **接口**: `DELETE /api/users/{id}`

#### 2.5 修改用户状态
- **接口**: `PUT /api/users/{id}/status?status={0|1}`

#### 2.6 重置用户密码
- **接口**: `PUT /api/users/{id}/password?newPassword={密码}`

---

### 3. 角色管理 (SysRoleController)

#### 3.1 查询所有角色
- **接口**: `GET /api/roles/list`

---

### 4. 商品分类管理 (CategoryController)

#### 4.1 分页查询分类列表
- **接口**: `GET /api/categories/list`
- **请求参数**:
  - `current`: 当前页
  - `size`: 每页条数
  - `categoryName`: 分类名称（可选）
  - `status`: 状态（可选）

#### 4.2 查询分类树
- **接口**: `GET /api/categories/tree`

#### 4.3 创建分类
- **接口**: `POST /api/categories`
- **请求体**:
```json
{
  "categoryName": "饮料",
  "parentId": 0,
  "sortOrder": 1,
  "description": "各类饮料",
  "status": 1
}
```

#### 4.4 更新分类
- **接口**: `PUT /api/categories`

#### 4.5 删除分类
- **接口**: `DELETE /api/categories/{id}`

---

### 5. 商品管理 (ProductController)

#### 5.1 分页查询商品列表
- **接口**: `GET /api/products/list`
- **请求参数**:
  - `current`: 当前页
  - `size`: 每页条数
  - `productName`: 商品名称（可选）
  - `categoryId`: 分类ID（可选）
  - `status`: 状态（可选）

#### 5.2 根据ID查询商品
- **接口**: `GET /api/products/{id}`

#### 5.3 创建商品
- **接口**: `POST /api/products`
- **请求体**:
```json
{
  "productName": "可口可乐",
  "productCode": "P001",
  "categoryId": 1,
  "specification": "330ml",
  "unit": "瓶",
  "costPrice": 2.50,
  "retailPrice": 3.50,
  "warningStock": 10,
  "imageUrl": "http://example.com/image.jpg",
  "description": "经典可乐",
  "status": 1
}
```

#### 5.4 更新商品
- **接口**: `PUT /api/products`

#### 5.5 删除商品
- **接口**: `DELETE /api/products/{id}`

#### 5.6 修改商品状态
- **接口**: `PUT /api/products/{id}/status?status={0|1}`

---

### 6. 供应商管理 (SupplierController)

#### 6.1 分页查询供应商列表
- **接口**: `GET /api/suppliers/list`

#### 6.2 查询所有启用的供应商
- **接口**: `GET /api/suppliers/enabled`

#### 6.3 创建供应商
- **接口**: `POST /api/suppliers`
- **请求体**:
```json
{
  "supplierName": "某某供应商",
  "contactName": "张三",
  "contactPhone": "13800138000",
  "address": "某某市某某区",
  "bankAccount": "6228480000000000000",
  "remark": "备注信息",
  "status": 1
}
```

#### 6.4 更新供应商
- **接口**: `PUT /api/suppliers`

#### 6.5 删除供应商
- **接口**: `DELETE /api/suppliers/{id}`

---

### 7. 采购管理 (PurchaseOrderController)

#### 7.1 分页查询采购订单列表
- **接口**: `GET /api/purchase/orders/list`
- **请求参数**:
  - `current`: 当前页
  - `size`: 每页条数
  - `orderNo`: 订单编号（可选）
  - `supplierId`: 供应商ID（可选）
  - `status`: 状态（可选，0-待审核，1-已通过，2-已拒绝，3-已入库）
  - `applicantId`: 申请人ID（可选）

#### 7.2 查询采购订单详情
- **接口**: `GET /api/purchase/orders/{id}`

#### 7.3 创建采购订单
- **接口**: `POST /api/purchase/orders?applicantId={userId}`
- **请求体**:
```json
{
  "supplierId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 100,
      "unitPrice": 2.50
    },
    {
      "productId": 2,
      "quantity": 50,
      "unitPrice": 5.00
    }
  ],
  "remark": "备注"
}
```

#### 7.4 审核采购订单
- **接口**: `PUT /api/purchase/orders/{id}/audit`
- **请求参数**:
  - `status`: 审核状态（1-通过，2-拒绝）
  - `auditRemark`: 审核备注（可选）
  - `auditorId`: 审核人ID

#### 7.5 确认入库
- **接口**: `PUT /api/purchase/orders/{id}/inbound?operatorId={userId}`

#### 7.6 删除采购订单
- **接口**: `DELETE /api/purchase/orders/{id}`

---

### 8. 销售管理 (SaleOrderController)

#### 8.1 分页查询销售订单列表
- **接口**: `GET /api/sale/orders/list`
- **请求参数**:
  - `current`: 当前页
  - `size`: 每页条数
  - `orderNo`: 订单编号（可选）
  - `customerId`: 客户ID（可选）
  - `status`: 状态（可选，0-待支付，1-已支付，2-已取消）

#### 8.2 查询销售订单详情
- **接口**: `GET /api/sale/orders/{id}`

#### 8.3 创建销售订单
- **接口**: `POST /api/sale/orders?cashierId={userId}`
- **请求体**:
```json
{
  "customerId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 2,
      "quantity": 3
    }
  ]
}
```

#### 8.4 支付订单
- **接口**: `PUT /api/sale/orders/{id}/pay`

#### 8.5 取消订单
- **接口**: `PUT /api/sale/orders/{id}/cancel`

#### 8.6 删除订单
- **接口**: `DELETE /api/sale/orders/{id}`

---

### 9. 库存管理 (InventoryController)

#### 9.1 分页查询库存列表
- **接口**: `GET /api/inventory/list`
- **请求参数**:
  - `current`: 当前页
  - `size`: 每页条数
  - `productName`: 商品名称（可选）
  - `isWarning`: 是否预警（可选，true/false）

#### 9.2 盘点调整
- **接口**: `PUT /api/inventory/adjust`
- **请求参数**:
  - `productId`: 商品ID
  - `newQuantity`: 新库存数量
  - `remark`: 备注（可选）
  - `operatorId`: 操作人ID

---

### 10. 统计分析 (StatisticsController)

#### 10.1 获取首页统计数据
- **接口**: `GET /api/statistics/dashboard`
- **响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "todaySales": 1250.50,
    "monthSales": 35000.00,
    "todayOrders": 25,
    "monthOrders": 680,
    "totalProducts": 150,
    "lowStockProducts": 12,
    "pendingPurchaseOrders": 5,
    "totalSuppliers": 20
  }
}
```

---

## 统一响应格式

所有接口都遵循统一的响应格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

### 响应状态码说明

- `200`: 操作成功
- `400`: 请求参数错误
- `401`: 未授权
- `403`: 无权限
- `404`: 资源不存在
- `500`: 服务器内部错误

---

## Apifox 使用建议

### 1. 环境配置
在 Apifox 中创建环境变量：
- `baseUrl`: `http://localhost:8080`
- `token`: 登录后获取的token（用于需要认证的接口）

### 2. 接口测试流程
1. 先调用登录接口获取 token
2. 将 token 保存到环境变量
3. 在需要认证的接口请求头中添加：`Authorization: Bearer {{token}}`
4. 按照业务流程测试各个接口

### 3. 测试用例建议
- 正常流程测试
- 异常参数测试（必填项为空、格式错误等）
- 边界值测试
- 权限测试

### 4. 数据准备
使用 `sql/data.sql` 中的初始化数据进行测试。

---

## 注意事项

1. 所有涉及用户操作的接口都需要传递操作人ID（applicantId、auditorId、operatorId等）
2. 删除操作请谨慎，建议先在测试环境验证
3. 订单创建后状态流转：待审核 → 已通过 → 已入库（采购）或 待支付 → 已支付（销售）
4. 库存操作会自动记录日志
5. 价格字段使用 BigDecimal，保留两位小数

---

## 联系方式

如有问题，请联系开发团队。



