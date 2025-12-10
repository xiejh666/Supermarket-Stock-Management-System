-- ========================================
--     数据库导入完整性验证SQL脚本
-- ========================================

-- 使用数据库
USE supermarket;

-- ========================================
-- 1. 检查数据库是否存在
-- ========================================
SELECT '数据库检查' AS '检查项';
SELECT SCHEMA_NAME AS '数据库名称', 
       DEFAULT_CHARACTER_SET_NAME AS '字符集',
       DEFAULT_COLLATION_NAME AS '排序规则'
FROM information_schema.SCHEMATA 
WHERE SCHEMA_NAME = 'supermarket';

-- ========================================
-- 2. 查看所有数据表
-- ========================================
SELECT '数据表列表' AS '检查项';
SHOW TABLES;

-- 统计表数量
SELECT COUNT(*) AS '数据表总数' 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'supermarket';

-- ========================================
-- 3. 检查关键表是否存在
-- ========================================
SELECT '关键表检查' AS '检查项';
SELECT TABLE_NAME AS '表名',
       CASE 
           WHEN TABLE_NAME IN ('sys_user', 'sys_role', 'product', 'category', 
                              'supplier', 'purchase_order', 'sale_order', 
                              'inventory', 'message_notification', 'system_config')
           THEN '✅ 存在'
           ELSE '⚠️  非关键表'
       END AS '状态'
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'supermarket'
ORDER BY TABLE_NAME;

-- ========================================
-- 4. 检查各表的数据量
-- ========================================
SELECT '数据量统计' AS '检查项';
SELECT 'sys_user' AS '表名', COUNT(*) AS '记录数' FROM sys_user
UNION ALL
SELECT 'sys_role', COUNT(*) FROM sys_role
UNION ALL
SELECT 'sys_permission', COUNT(*) FROM sys_permission
UNION ALL
SELECT 'product', COUNT(*) FROM product
UNION ALL
SELECT 'category', COUNT(*) FROM category
UNION ALL
SELECT 'supplier', COUNT(*) FROM supplier
UNION ALL
SELECT 'purchase_order', COUNT(*) FROM purchase_order
UNION ALL
SELECT 'purchase_order_item', COUNT(*) FROM purchase_order_item
UNION ALL
SELECT 'sale_order', COUNT(*) FROM sale_order
UNION ALL
SELECT 'sale_order_item', COUNT(*) FROM sale_order_item
UNION ALL
SELECT 'inventory', COUNT(*) FROM inventory
UNION ALL
SELECT 'inventory_log', COUNT(*) FROM inventory_log
UNION ALL
SELECT 'customer', COUNT(*) FROM customer
UNION ALL
SELECT 'message_notification', COUNT(*) FROM message_notification
UNION ALL
SELECT 'system_config', COUNT(*) FROM system_config
UNION ALL
SELECT 'user_config', COUNT(*) FROM user_config
ORDER BY 记录数 DESC;

-- ========================================
-- 5. 检查表结构（以sys_user为例）
-- ========================================
SELECT '表结构检查 (sys_user)' AS '检查项';
DESCRIBE sys_user;

-- ========================================
-- 6. 检查数据完整性 - 用户数据
-- ========================================
SELECT '用户数据检查' AS '检查项';
SELECT id, username, real_name, phone, email, role_id, status, create_time
FROM sys_user
ORDER BY id
LIMIT 10;

-- 检查用户角色关联
SELECT '用户角色关联检查' AS '检查项';
SELECT u.id, u.username, u.real_name, r.role_name, r.role_code
FROM sys_user u
LEFT JOIN sys_role r ON u.role_id = r.id
LIMIT 10;

-- ========================================
-- 7. 检查数据完整性 - 商品数据
-- ========================================
SELECT '商品数据检查' AS '检查项';
SELECT id, product_code, product_name, category_id, unit, cost_price, price, status
FROM product
ORDER BY id
LIMIT 10;

-- 检查商品分类关联
SELECT '商品分类关联检查' AS '检查项';
SELECT p.id, p.product_code, p.product_name, c.category_name
FROM product p
LEFT JOIN category c ON p.category_id = c.id
LIMIT 10;

-- ========================================
-- 8. 检查订单数据
-- ========================================
SELECT '采购订单检查' AS '检查项';
SELECT id, order_code, supplier_id, total_amount, status, create_time
FROM purchase_order
ORDER BY id DESC
LIMIT 5;

SELECT '销售订单检查' AS '检查项';
SELECT id, order_code, customer_id, total_amount, status, create_time
FROM sale_order
ORDER BY id DESC
LIMIT 5;

-- ========================================
-- 9. 检查库存数据
-- ========================================
SELECT '库存数据检查' AS '检查项';
SELECT i.id, p.product_name, i.quantity, i.warning_quantity,
       CASE 
           WHEN i.quantity <= i.warning_quantity THEN '⚠️  库存不足'
           ELSE '✅ 正常'
       END AS '库存状态'
FROM inventory i
LEFT JOIN product p ON i.product_id = p.id
ORDER BY i.quantity ASC
LIMIT 10;

-- ========================================
-- 10. 检查外键关系完整性
-- ========================================
SELECT '外键关系检查' AS '检查项';

-- 检查孤立用户（角色不存在）
SELECT '孤立用户（角色不存在）' AS '问题类型', COUNT(*) AS '数量'
FROM sys_user u
LEFT JOIN sys_role r ON u.role_id = r.id
WHERE r.id IS NULL AND u.role_id IS NOT NULL;

-- 检查孤立商品（分类不存在）
SELECT '孤立商品（分类不存在）' AS '问题类型', COUNT(*) AS '数量'
FROM product p
LEFT JOIN category c ON p.category_id = c.id
WHERE c.id IS NULL AND p.category_id IS NOT NULL;

-- 检查孤立采购订单（供应商不存在）
SELECT '孤立采购订单（供应商不存在）' AS '问题类型', COUNT(*) AS '数量'
FROM purchase_order po
LEFT JOIN supplier s ON po.supplier_id = s.id
WHERE s.id IS NULL AND po.supplier_id IS NOT NULL;

-- ========================================
-- 11. 检查字符集和排序规则
-- ========================================
SELECT '字符集检查' AS '检查项';
SELECT TABLE_NAME AS '表名',
       TABLE_COLLATION AS '排序规则'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'supermarket'
ORDER BY TABLE_NAME;

-- ========================================
-- 12. 检查索引
-- ========================================
SELECT '索引检查 (sys_user)' AS '检查项';
SHOW INDEX FROM sys_user;

-- ========================================
-- 13. 数据统计汇总
-- ========================================
SELECT '数据统计汇总' AS '检查项';
SELECT 
    (SELECT COUNT(*) FROM sys_user) AS '用户数',
    (SELECT COUNT(*) FROM sys_role) AS '角色数',
    (SELECT COUNT(*) FROM product) AS '商品数',
    (SELECT COUNT(*) FROM category) AS '分类数',
    (SELECT COUNT(*) FROM supplier) AS '供应商数',
    (SELECT COUNT(*) FROM purchase_order) AS '采购订单数',
    (SELECT COUNT(*) FROM sale_order) AS '销售订单数',
    (SELECT COUNT(*) FROM inventory) AS '库存记录数',
    (SELECT COUNT(*) FROM message_notification) AS '消息数';

-- ========================================
-- 14. 检查最近的数据
-- ========================================
SELECT '最近创建的数据' AS '检查项';
SELECT '最近用户' AS '类型', username AS '名称', create_time AS '创建时间'
FROM sys_user
ORDER BY create_time DESC
LIMIT 5
UNION ALL
SELECT '最近商品', product_name, create_time
FROM product
ORDER BY create_time DESC
LIMIT 5
UNION ALL
SELECT '最近采购订单', order_code, create_time
FROM purchase_order
ORDER BY create_time DESC
LIMIT 5;

-- ========================================
-- 15. 验证完成提示
-- ========================================
SELECT '✅ 验证完成！' AS '状态',
       '如果以上查询都正常返回数据，说明数据库导入成功。' AS '说明';

