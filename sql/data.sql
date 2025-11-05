-- 超市进销存管理系统初始数据脚本
USE `supermarket_db`;

-- ============================================
-- 1. 角色数据
-- ============================================
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`) VALUES
(1, '管理员', 'ADMIN', '系统管理员，拥有所有权限'),
(2, '采购员', 'PURCHASER', '负责采购业务'),
(3, '营业员', 'CASHIER', '负责销售业务');

-- ============================================
-- 2. 权限数据（基础权限）
-- ============================================
-- 一级菜单
INSERT INTO `sys_permission` (`id`, `permission_name`, `permission_code`, `parent_id`, `permission_type`, `path`, `icon`, `sort_order`) VALUES
(1, '系统管理', 'system', 0, 1, '/system', 'Setting', 1),
(2, '基础数据', 'base', 0, 1, '/base', 'Box', 2),
(3, '采购管理', 'purchase', 0, 1, '/purchase', 'ShoppingCart', 3),
(4, '销售管理', 'sale', 0, 1, '/sale', 'Goods', 4),
(5, '库存管理', 'inventory', 0, 1, '/inventory', 'Box', 5),
(6, '统计分析', 'statistics', 0, 1, '/statistics', 'DataAnalysis', 6);

-- 二级菜单 - 系统管理
INSERT INTO `sys_permission` (`id`, `permission_name`, `permission_code`, `parent_id`, `permission_type`, `path`, `icon`, `sort_order`) VALUES
(10, '员工管理', 'system:user', 1, 1, '/system/user', 'User', 1),
(11, '角色管理', 'system:role', 1, 1, '/system/role', 'UserFilled', 2),
(12, '权限管理', 'system:permission', 1, 1, '/system/permission', 'Lock', 3);

-- 二级菜单 - 基础数据
INSERT INTO `sys_permission` (`id`, `permission_name`, `permission_code`, `parent_id`, `permission_type`, `path`, `icon`, `sort_order`) VALUES
(20, '商品分类', 'base:category', 2, 1, '/base/category', 'Folder', 1),
(21, '商品管理', 'base:product', 2, 1, '/base/product', 'Goods', 2),
(22, '供应商管理', 'base:supplier', 2, 1, '/base/supplier', 'Shop', 3);

-- 二级菜单 - 采购管理
INSERT INTO `sys_permission` (`id`, `permission_name`, `permission_code`, `parent_id`, `permission_type`, `path`, `icon`, `sort_order`) VALUES
(30, '采购申请', 'purchase:apply', 3, 1, '/purchase/apply', 'DocumentAdd', 1),
(31, '采购审核', 'purchase:audit', 3, 1, '/purchase/audit', 'Checked', 2),
(32, '采购入库', 'purchase:confirm', 3, 1, '/purchase/confirm', 'Box', 3);

-- 二级菜单 - 销售管理
INSERT INTO `sys_permission` (`id`, `permission_name`, `permission_code`, `parent_id`, `permission_type`, `path`, `icon`, `sort_order`) VALUES
(40, '商品销售', 'sale:sell', 4, 1, '/sale/sell', 'ShoppingBag', 1),
(41, '订单管理', 'sale:order', 4, 1, '/sale/order', 'Document', 2),
(42, '客户管理', 'sale:customer', 4, 1, '/sale/customer', 'User', 3);

-- 二级菜单 - 库存管理
INSERT INTO `sys_permission` (`id`, `permission_name`, `permission_code`, `parent_id`, `permission_type`, `path`, `icon`, `sort_order`) VALUES
(50, '库存查询', 'inventory:query', 5, 1, '/inventory/query', 'Search', 1),
(51, '库存预警', 'inventory:warning', 5, 1, '/inventory/warning', 'Warning', 2),
(52, '库存日志', 'inventory:log', 5, 1, '/inventory/log', 'Document', 3);

-- 二级菜单 - 统计分析
INSERT INTO `sys_permission` (`id`, `permission_name`, `permission_code`, `parent_id`, `permission_type`, `path`, `icon`, `sort_order`) VALUES
(60, '销售统计', 'statistics:sale', 6, 1, '/statistics/sale', 'TrendCharts', 1),
(61, '库存统计', 'statistics:inventory', 6, 1, '/statistics/inventory', 'DataLine', 2);

-- ============================================
-- 3. 角色权限关联（管理员拥有所有权限）
-- ============================================
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, id FROM `sys_permission`;

-- 采购员权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(2, 30), (2, 32), (2, 50), (2, 51);

-- 营业员权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(3, 40), (3, 41), (3, 42);

-- ============================================
-- 4. 用户数据（密码：123456，使用BCrypt加密后的值）
-- ============================================
-- 注意：实际密码需要使用BCrypt加密，这里只是示例
-- 使用BCrypt加密 "123456" 的结果示例（实际使用时需要后端加密）
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `phone`, `status`, `role_id`) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pDPW', '管理员', '13800138000', 1, 1),
(2, 'purchaser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pDPW', '采购员', '13800138001', 1, 2),
(3, 'cashier', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pDPW', '营业员', '13800138002', 1, 3);

-- ============================================
-- 5. 商品分类数据
-- ============================================
INSERT INTO `category` (`id`, `category_name`, `parent_id`, `sort_order`) VALUES
(1, '食品饮料', 0, 1),
(2, '日用品', 0, 2),
(3, '生鲜', 0, 3),
(11, '零食', 1, 1),
(12, '饮料', 1, 2),
(21, '洗护用品', 2, 1),
(22, '清洁用品', 2, 2);

-- ============================================
-- 6. 供应商数据
-- ============================================
INSERT INTO `supplier` (`id`, `supplier_name`, `contact_person`, `phone`, `address`, `status`) VALUES
(1, '食品供应商A', '张经理', '13900139000', '北京市朝阳区xxx', 1),
(2, '日用品供应商B', '李经理', '13900139001', '上海市浦东新区xxx', 1),
(3, '生鲜供应商C', '王经理', '13900139002', '广州市天河区xxx', 1);

-- ============================================
-- 7. 商品数据
-- ============================================
INSERT INTO `product` (`id`, `product_code`, `product_name`, `category_id`, `supplier_id`, `unit`, `price`, `cost_price`, `status`) VALUES
(1, 'P001', '可口可乐', 12, 1, '瓶', 3.50, 2.50, 1),
(2, 'P002', '康师傅方便面', 11, 1, '袋', 4.50, 3.00, 1),
(3, 'P003', '洗衣液', 21, 2, '瓶', 25.00, 18.00, 1),
(4, 'P004', '苹果', 3, 3, '斤', 8.00, 5.00, 1);

-- ============================================
-- 8. 初始化库存数据
-- ============================================
INSERT INTO `inventory` (`product_id`, `quantity`, `warning_quantity`) VALUES
(1, 100, 20),
(2, 50, 10),
(3, 30, 5),
(4, 80, 15);


