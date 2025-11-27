# 权限控制和消息通知系统实现指南

## 一、数据库初始化

### 1. 创建系统通知表

请在MySQL中执行以下SQL语句：

```sql
-- 系统通知表
CREATE TABLE IF NOT EXISTS `system_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(200) NOT NULL COMMENT '通知标题',
  `content` text NOT NULL COMMENT '通知内容',
  `type` varchar(50) NOT NULL COMMENT '通知类型：PURCHASE_AUDIT-采购审核，SALE_PAYMENT-销售支付，SYSTEM-系统通知',
  `receiver_id` bigint DEFAULT NULL COMMENT '接收用户ID（为空表示发给所有管理员）',
  `sender_id` bigint NOT NULL COMMENT '发送用户ID',
  `business_id` bigint DEFAULT NULL COMMENT '关联业务ID（如采购订单ID、销售订单ID）',
  `business_type` varchar(50) DEFAULT NULL COMMENT '业务类型：PURCHASE_ORDER-采购订单，SALE_ORDER-销售订单',
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '是否已读：0-未读，1-已读',
  `priority` tinyint NOT NULL DEFAULT '2' COMMENT '优先级：1-低，2-中，3-高',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_business` (`business_id`, `business_type`),
  KEY `idx_type_read` (`type`, `is_read`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知表';
```

### 2. 添加角色数据

确保sys_role表中有以下角色：

```sql
-- 插入角色数据（如果不存在）
INSERT INTO `sys_role` (`role_name`, `role_code`, `description`) 
VALUES 
  ('管理员', 'ADMIN', '系统管理员，拥有所有权限'),
  ('采购员', 'PURCHASER', '负责采购管理'),
  ('销售员', 'SALESPERSON', '负责销售管理')
ON DUPLICATE KEY UPDATE role_name=VALUES(role_name);
```

## 二、后端实现说明

### 已创建的文件：

1. **实体类**
   - `SystemNotification.java` - 系统通知实体
   - `UserStatusVO.java` - 用户状态VO
   - `SystemNotificationVO.java` - 系统通知VO

2. **Mapper**
   - `SystemNotificationMapper.java` - 通知Mapper接口
   - `SystemNotificationMapper.xml` - 通知Mapper XML

3. **Service**
   - `SystemNotificationService.java` - 通知服务接口
   - `SystemNotificationServiceImpl.java` - 通知服务实现

4. **Controller**
   - `SystemNotificationController.java` - 通知控制器

5. **已修改的文件**
   - `PurchaseOrderServiceImpl.java` - 添加了创建采购订单后发送通知
   - `SaleOrderServiceImpl.java` - 添加了支付销售订单后发送通知
   - `SysUserController.java` - 添加了获取用户状态接口
   - `SysUserService.java` - 添加了getUserStatus方法
   - `SysUserServiceImpl.java` - 实现了getUserStatus方法

## 三、前端实现说明

### 已创建的文件：

1. **工具类**
   - `permission.js` - 权限控制工具
   - `userStatusCheck.js` - 用户状态检查工具

2. **API**
   - `notification.js` - 通知API

### 需要修改的文件：

1. **Layout.vue** - 需要将消息通知改为使用新的notification API
2. **各业务页面** - 需要添加权限控制

## 四、权限控制使用方法

### 1. 在Vue组件中使用权限控制

```javascript
import { canCreate, canUpdate, canDelete, checkPermission } from '@/utils/permission'

// 检查是否有创建权限
if (canCreate('purchase')) {
  // 执行创建操作
}

// 使用checkPermission包装操作
const handleDelete = (row) => {
  checkPermission('delete', 'purchase', () => {
    // 执行删除操作
  })
}
```

### 2. 在模板中使用权限控制

```vue
<el-button 
  v-if="canCreate('purchase')" 
  type="primary" 
  @click="handleAdd">
  新增
</el-button>

<el-button 
  type="danger" 
  @click="() => checkPermission('delete', 'purchase', () => handleDelete(row))">
  删除
</el-button>
```

### 3. 权限资源名称

- `dashboard` - 仪表盘
- `category` - 分类管理
- `product` - 商品管理
- `supplier` - 供应商管理
- `customer` - 客户管理
- `purchase` - 采购管理
- `sale` - 销售管理
- `inventory` - 库存管理
- `user` - 用户管理

## 五、消息通知工作流程

### 1. 采购流程

```
采购员创建采购订单（状态：待审核）
    ↓
系统自动发送通知给所有管理员
    ↓
管理员在消息通知中看到提醒
    ↓
管理员审核采购订单
    ↓
审核通过：状态变为待入库
审核拒绝：状态变为已拒绝
```

### 2. 销售流程

```
销售员创建销售订单（状态：待支付）
    ↓
销售员确认支付
    ↓
系统自动发送通知给所有管理员
    ↓
管理员在消息通知中看到销售记录
```

## 六、测试步骤

### 1. 测试权限控制

1. 创建采购员账号（roleCode: PURCHASER）
2. 创建销售员账号（roleCode: SALESPERSON）
3. 使用采购员登录，尝试：
   - ✅ 可以查看采购管理
   - ✅ 可以创建采购订单
   - ❌ 不能删除其他模块数据
   - ❌ 不能访问用户管理
4. 使用销售员登录，尝试：
   - ✅ 可以查看销售管理
   - ✅ 可以创建销售订单
   - ❌ 不能删除其他模块数据
   - ❌ 不能访问用户管理

### 2. 测试消息通知

1. 使用采购员登录，创建一个采购订单
2. 使用管理员登录，查看：
   - ✅ 消息通知图标显示未读数量
   - ✅ 点击查看通知详情
   - ✅ 通知内容包含采购员姓名、订单号、金额
3. 使用销售员登录，创建销售订单并确认支付
4. 使用管理员登录，查看：
   - ✅ 消息通知图标显示未读数量
   - ✅ 点击查看通知详情
   - ✅ 通知内容包含销售员姓名、订单号、金额

### 3. 测试最新动态

1. 管理员登录后，在首页仪表盘查看"最新动态"
2. 应该显示最近的采购和销售通知
3. 点击通知可以跳转到对应的订单详情

## 七、下一步工作

1. **修改Layout.vue** - 将消息通知改为使用notification API
2. **修改各业务页面** - 添加权限控制按钮
3. **完善仪表盘** - 显示最新动态
4. **添加审核页面** - 管理员可以直接在通知中审核采购订单

## 八、注意事项

1. 确保数据库表已创建
2. 确保角色数据已正确配置
3. 重启后端服务以加载新的代码
4. 清除浏览器缓存后测试前端功能
5. 检查控制台是否有错误信息
