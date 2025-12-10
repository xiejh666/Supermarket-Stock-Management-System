-- ====================================
-- 超市库存管理系统 - 数据库结构
-- 导出时间: 2025-12-10 09:08:32
-- 数据库: supermarket_db
-- ====================================

CREATE DATABASE IF NOT EXISTS `supermarket_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `supermarket_db`;

-- ====================================
-- 表结构: category
-- ====================================
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` varchar(50) NOT NULL COMMENT '分类名称',
  `parent_id` bigint DEFAULT '0' COMMENT '父分类ID',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类表';

-- 字段说明:
-- id: bigint [主键] - 分类ID
-- category_name: varchar(50) - 分类名称
-- parent_id: bigint [索引] - 父分类ID
-- sort_order: int - 排序
-- description: varchar(255) - 描述
-- create_time: datetime - 创建时间
-- update_time: datetime - 更新时间

-- ====================================
-- 表结构: customer
-- ====================================
DROP TABLE IF EXISTS `customer`;
CREATE TABLE `customer` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '客户ID',
  `customer_name` varchar(50) NOT NULL COMMENT '客户姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户表';

-- 字段说明:
-- id: bigint [主键] - 客户ID
-- customer_name: varchar(50) - 客户姓名
-- phone: varchar(20) - 联系电话
-- address: varchar(255) - 地址
-- create_time: datetime - 创建时间
-- update_time: datetime - 更新时间

-- ====================================
-- 表结构: inventory
-- ====================================
DROP TABLE IF EXISTS `inventory`;
CREATE TABLE `inventory` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `quantity` int DEFAULT '0' COMMENT '库存数量',
  `warning_quantity` int DEFAULT '10' COMMENT '预警数量',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_id` (`product_id`),
  KEY `idx_quantity` (`quantity`),
  CONSTRAINT `fk_inventory_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=86 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存表';

-- 字段说明:
-- id: bigint [主键] - ID
-- product_id: bigint [唯一] - 商品ID
-- quantity: int [索引] - 库存数量
-- warning_quantity: int - 预警数量
-- update_time: datetime - 更新时间

-- ====================================
-- 表结构: inventory_log
-- ====================================
DROP TABLE IF EXISTS `inventory_log`;
CREATE TABLE `inventory_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `change_type` tinyint NOT NULL COMMENT '变动类型：1-入库，2-出库，3-盘点调整',
  `change_quantity` int NOT NULL COMMENT '变动数量（正数表示增加，负数表示减少）',
  `before_quantity` int NOT NULL COMMENT '变动前数量',
  `after_quantity` int NOT NULL COMMENT '变动后数量',
  `order_no` varchar(50) DEFAULT NULL COMMENT '关联订单号',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_inventory_log_operator` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_inventory_log_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存变动日志表';

-- 字段说明:
-- id: bigint [主键] - ID
-- product_id: bigint [索引] - 商品ID
-- change_type: tinyint - 变动类型：1-入库，2-出库，3-盘点调整
-- change_quantity: int - 变动数量（正数表示增加，负数表示减少）
-- before_quantity: int - 变动前数量
-- after_quantity: int - 变动后数量
-- order_no: varchar(50) - 关联订单号
-- remark: varchar(255) - 备注
-- operator_id: bigint [索引] - 操作人ID
-- create_time: datetime [索引] - 创建时间

-- ====================================
-- 表结构: message_notification
-- ====================================
DROP TABLE IF EXISTS `message_notification`;
CREATE TABLE `message_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `user_id` bigint DEFAULT NULL COMMENT '接收用户ID（NULL表示所有人可见）',
  `role_code` varchar(50) DEFAULT NULL COMMENT '接收角色编码（NULL表示所有角色可见）',
  `type` varchar(20) NOT NULL COMMENT '消息类型：success/warning/info/error',
  `category` varchar(50) NOT NULL COMMENT '消息分类：system/purchase/sale/inventory/user',
  `title` varchar(200) NOT NULL COMMENT '消息标题',
  `content` text NOT NULL COMMENT '消息内容',
  `link_type` varchar(50) DEFAULT NULL COMMENT '跳转类型：purchase/sale/inventory/product/supplier',
  `link_id` bigint DEFAULT NULL COMMENT '关联业务ID',
  `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已读：0-未读，1-已读',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_code` (`role_code`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息通知表';

-- 字段说明:
-- id: bigint [主键] - 消息ID
-- user_id: bigint [索引] - 接收用户ID（NULL表示所有人可见）
-- role_code: varchar(50) [索引] - 接收角色编码（NULL表示所有角色可见）
-- type: varchar(20) - 消息类型：success/warning/info/error
-- category: varchar(50) - 消息分类：system/purchase/sale/inventory/user
-- title: varchar(200) - 消息标题
-- content: text - 消息内容
-- link_type: varchar(50) - 跳转类型：purchase/sale/inventory/product/supplier
-- link_id: bigint - 关联业务ID
-- is_read: tinyint(1) [索引] - 是否已读：0-未读，1-已读
-- read_time: datetime - 阅读时间
-- create_time: datetime [索引] - 创建时间
-- update_time: datetime - 更新时间

-- ====================================
-- 表结构: product
-- ====================================
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `product_code` varchar(50) NOT NULL COMMENT '商品编码',
  `product_name` varchar(100) NOT NULL COMMENT '商品名称',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `supplier_id` bigint DEFAULT NULL COMMENT '供应商ID',
  `unit` varchar(20) DEFAULT '件' COMMENT '单位',
  `specification` varchar(50) DEFAULT NULL COMMENT '规格',
  `price` decimal(10,2) NOT NULL COMMENT '售价',
  `cost_price` decimal(10,2) DEFAULT NULL COMMENT '成本价',
  `image` varchar(255) DEFAULT NULL COMMENT '商品图片',
  `description` varchar(500) DEFAULT NULL COMMENT '商品描述',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-下架，1-上架',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_code` (`product_code`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_supplier_id` (`supplier_id`),
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`),
  CONSTRAINT `fk_product_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=86 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品表';

-- 字段说明:
-- id: bigint [主键] - 商品ID
-- product_code: varchar(50) [唯一] - 商品编码
-- product_name: varchar(100) - 商品名称
-- category_id: bigint [索引] - 分类ID
-- supplier_id: bigint [索引] - 供应商ID
-- unit: varchar(20) - 单位
-- specification: varchar(50) - 规格
-- price: decimal(10,2) - 售价
-- cost_price: decimal(10,2) - 成本价
-- image: varchar(255) - 商品图片
-- description: varchar(500) - 商品描述
-- status: tinyint - 状态：0-下架，1-上架
-- create_time: datetime - 创建时间
-- update_time: datetime - 更新时间

-- ====================================
-- 表结构: purchase_order
-- ====================================
DROP TABLE IF EXISTS `purchase_order`;
CREATE TABLE `purchase_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '采购订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单编号',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `total_amount` decimal(10,2) DEFAULT '0.00' COMMENT '订单总金额',
  `purchase_date` date DEFAULT NULL COMMENT '采购日期',
  `status` tinyint DEFAULT '0' COMMENT '订单状态：0-待审核，1-已通过，2-已拒绝，3-已入库',
  `applicant_id` bigint NOT NULL COMMENT '申请人ID',
  `auditor_id` bigint DEFAULT NULL COMMENT '审核人ID',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(500) DEFAULT NULL COMMENT '审核备注',
  `inbound_time` datetime DEFAULT NULL COMMENT '入库时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_applicant_id` (`applicant_id`),
  KEY `idx_status` (`status`),
  KEY `fk_purchase_auditor` (`auditor_id`),
  CONSTRAINT `fk_purchase_applicant` FOREIGN KEY (`applicant_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_purchase_auditor` FOREIGN KEY (`auditor_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_purchase_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=130 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购订单表';

-- 字段说明:
-- id: bigint [主键] - 采购订单ID
-- order_no: varchar(50) [唯一] - 订单编号
-- supplier_id: bigint [索引] - 供应商ID
-- total_amount: decimal(10,2) - 订单总金额
-- purchase_date: date - 采购日期
-- status: tinyint [索引] - 订单状态：0-待审核，1-已通过，2-已拒绝，3-已入库
-- applicant_id: bigint [索引] - 申请人ID
-- auditor_id: bigint [索引] - 审核人ID
-- audit_time: datetime - 审核时间
-- audit_remark: varchar(500) - 审核备注
-- inbound_time: datetime - 入库时间
-- create_time: datetime - 创建时间
-- update_time: datetime - 更新时间

-- ====================================
-- 表结构: purchase_order_item
-- ====================================
DROP TABLE IF EXISTS `purchase_order_item`;
CREATE TABLE `purchase_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `quantity` int NOT NULL COMMENT '数量',
  `unit_price` decimal(10,2) NOT NULL COMMENT '单价',
  `total_price` decimal(10,2) NOT NULL COMMENT '总价',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`),
  CONSTRAINT `fk_purchase_item_order` FOREIGN KEY (`order_id`) REFERENCES `purchase_order` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_purchase_item_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=405 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购订单明细表';

-- 字段说明:
-- id: bigint [主键] - ID
-- order_id: bigint [索引] - 订单ID
-- product_id: bigint [索引] - 商品ID
-- quantity: int - 数量
-- unit_price: decimal(10,2) - 单价
-- total_price: decimal(10,2) - 总价
-- create_time: datetime - 创建时间

-- ====================================
-- 表结构: sale_order
-- ====================================
DROP TABLE IF EXISTS `sale_order`;
CREATE TABLE `sale_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '销售订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单编号',
  `customer_id` bigint DEFAULT NULL COMMENT '客户ID',
  `total_amount` decimal(10,2) DEFAULT '0.00' COMMENT '订单总金额',
  `status` tinyint DEFAULT '0' COMMENT '订单状态：0-待支付，1-已支付，2-已取消',
  `cashier_id` bigint NOT NULL COMMENT '收银员ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `cancel_reason` varchar(255) DEFAULT NULL COMMENT '取消原因',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_cashier_id` (`cashier_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_sale_cashier` FOREIGN KEY (`cashier_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_sale_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=617 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='销售订单表';

-- 字段说明:
-- id: bigint [主键] - 销售订单ID
-- order_no: varchar(50) [唯一] - 订单编号
-- customer_id: bigint [索引] - 客户ID
-- total_amount: decimal(10,2) - 订单总金额
-- status: tinyint [索引] - 订单状态：0-待支付，1-已支付，2-已取消
-- cashier_id: bigint [索引] - 收银员ID
-- create_time: datetime - 创建时间
-- update_time: datetime - 更新时间
-- cancel_reason: varchar(255) - 取消原因

-- ====================================
-- 表结构: sale_order_item
-- ====================================
DROP TABLE IF EXISTS `sale_order_item`;
CREATE TABLE `sale_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `quantity` int NOT NULL COMMENT '数量',
  `unit_price` decimal(10,2) NOT NULL COMMENT '单价',
  `total_price` decimal(10,2) NOT NULL COMMENT '总价',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`),
  CONSTRAINT `fk_sale_item_order` FOREIGN KEY (`order_id`) REFERENCES `sale_order` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_sale_item_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2581 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='销售订单明细表';

-- 字段说明:
-- id: bigint [主键] - ID
-- order_id: bigint [索引] - 订单ID
-- product_id: bigint [索引] - 商品ID
-- quantity: int - 数量
-- unit_price: decimal(10,2) - 单价
-- total_price: decimal(10,2) - 总价
-- create_time: datetime - 创建时间

-- ====================================
-- 表结构: supplier
-- ====================================
DROP TABLE IF EXISTS `supplier`;
CREATE TABLE `supplier` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '供应商ID',
  `supplier_name` varchar(100) NOT NULL COMMENT '供应商名称',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商表';

-- 字段说明:
-- id: bigint [主键] - 供应商ID
-- supplier_name: varchar(100) - 供应商名称
-- contact_person: varchar(50) - 联系人
-- phone: varchar(20) - 联系电话
-- address: varchar(255) - 地址
-- status: tinyint - 状态：0-禁用，1-启用
-- create_time: datetime - 创建时间
-- update_time: datetime - 更新时间

-- ====================================
-- 表结构: sys_permission
-- ====================================
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `permission_name` varchar(50) NOT NULL COMMENT '权限名称',
  `permission_code` varchar(100) NOT NULL COMMENT '权限编码',
  `parent_id` bigint DEFAULT '0' COMMENT '父权限ID',
  `permission_type` tinyint DEFAULT '1' COMMENT '权限类型：1-菜单，2-按钮',
  `path` varchar(255) DEFAULT NULL COMMENT '路由路径',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限表';

-- 字段说明:
-- id: bigint [主键] - 权限ID
-- permission_name: varchar(50) - 权限名称
-- permission_code: varchar(100) [唯一] - 权限编码
-- parent_id: bigint - 父权限ID
-- permission_type: tinyint - 权限类型：1-菜单，2-按钮
-- path: varchar(255) - 路由路径
-- icon: varchar(50) - 图标
-- sort_order: int - 排序
-- create_time: datetime - 创建时间
-- update_time: datetime - 更新时间

-- ====================================
-- 表结构: sys_role
-- ====================================
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `description` varchar(255) DEFAULT NULL COMMENT '角色描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

-- 字段说明:
-- id: bigint [主键] - 角色ID
-- role_name: varchar(50) - 角色名称
-- role_code: varchar(50) [唯一] - 角色编码
-- description: varchar(255) - 角色描述
-- create_time: datetime - 创建时间
-- update_time: datetime - 更新时间

-- ====================================
-- 表结构: sys_role_permission
-- ====================================
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`,`permission_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`),
  CONSTRAINT `fk_role_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `sys_permission` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_role_permission_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色权限关联表';

-- 字段说明:
-- id: bigint [主键] - ID
-- role_id: bigint [索引] - 角色ID
-- permission_id: bigint [索引] - 权限ID
-- create_time: datetime - 创建时间

-- ====================================
-- 表结构: sys_user
-- ====================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(500) DEFAULT NULL COMMENT '用户头像URL',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `role_id` bigint DEFAULT NULL COMMENT '角色ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role_id` (`role_id`),
  CONSTRAINT `fk_user_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- 字段说明:
-- id: bigint [主键] - 用户ID
-- username: varchar(50) [唯一] - 用户名
-- password: varchar(255) - 密码
-- real_name: varchar(50) - 真实姓名
-- phone: varchar(20) - 手机号
-- email: varchar(100) - 邮箱
-- avatar: varchar(500) - 用户头像URL
-- status: tinyint - 状态：0-禁用，1-启用
-- role_id: bigint [索引] - 角色ID
-- create_time: datetime - 创建时间
-- update_time: datetime - 更新时间

-- ====================================
-- 表结构: system_config
-- ====================================
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值',
  `config_desc` varchar(200) DEFAULT NULL COMMENT '配置描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置表';

-- 字段说明:
-- id: bigint [主键] - 配置ID
-- config_key: varchar(100) [唯一] - 配置键
-- config_value: text - 配置值
-- config_desc: varchar(200) - 配置描述
-- create_time: datetime - 创建时间
-- update_time: datetime - 更新时间

-- ====================================
-- 表结构: system_notification
-- ====================================
DROP TABLE IF EXISTS `system_notification`;
CREATE TABLE `system_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL,
  `content` text NOT NULL,
  `type` varchar(50) NOT NULL,
  `receiver_id` bigint DEFAULT NULL,
  `sender_id` bigint NOT NULL,
  `business_id` bigint DEFAULT NULL,
  `business_type` varchar(50) DEFAULT NULL,
  `related_code` varchar(100) DEFAULT NULL COMMENT '关联编码（如商品编码、订单号等）',
  `is_read` tinyint NOT NULL DEFAULT '0',
  `priority` tinyint NOT NULL DEFAULT '2',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_sender_id` (`sender_id`)
) ENGINE=InnoDB AUTO_INCREMENT=91 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 字段说明:
-- id: bigint [主键]
-- title: varchar(200)
-- content: text
-- type: varchar(50)
-- receiver_id: bigint [索引]
-- sender_id: bigint [索引]
-- business_id: bigint
-- business_type: varchar(50)
-- related_code: varchar(100) - 关联编码（如商品编码、订单号等）
-- is_read: tinyint
-- priority: tinyint
-- create_time: datetime
-- update_time: datetime

-- ====================================
-- 表结构: user_config
-- ====================================
DROP TABLE IF EXISTS `user_config`;
CREATE TABLE `user_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` varchar(500) DEFAULT NULL COMMENT '配置值',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_config` (`user_id`,`config_key`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户配置表';

-- 字段说明:
-- id: bigint [主键] - 配置ID
-- user_id: bigint [索引] - 用户ID
-- config_key: varchar(100) - 配置键
-- config_value: varchar(500) - 配置值
-- create_time: datetime - 创建时间
-- update_time: datetime - 更新时间

