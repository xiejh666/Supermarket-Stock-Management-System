-- 插入测试数据（如果 data.sql 没有导入成功）
USE supermarket_db;

-- 1. 角色数据
INSERT INTO sys_role (id, role_name, role_code, description) VALUES
(1, '管理员', 'ADMIN', '系统管理员，拥有所有权限'),
(2, '采购员', 'PURCHASER', '负责采购业务'),
(3, '营业员', 'CASHIER', '负责销售业务')
ON DUPLICATE KEY UPDATE 
    role_name = VALUES(role_name);

-- 2. 商品分类
INSERT INTO category (id, category_name, parent_id, sort_order) VALUES
(1, '食品饮料', 0, 1),
(2, '日用品', 0, 2),
(3, '生鲜', 0, 3),
(11, '零食', 1, 1),
(12, '饮料', 1, 2),
(21, '洗护用品', 2, 1),
(22, '清洁用品', 2, 2)
ON DUPLICATE KEY UPDATE 
    category_name = VALUES(category_name);

-- 3. 供应商
INSERT INTO supplier (id, supplier_name, contact_person, phone, address, status) VALUES
(1, '食品供应商A', '张经理', '13900139000', '北京市朝阳区xxx', 1),
(2, '日用品供应商B', '李经理', '13900139001', '上海市浦东新区xxx', 1),
(3, '生鲜供应商C', '王经理', '13900139002', '广州市天河区xxx', 1)
ON DUPLICATE KEY UPDATE 
    supplier_name = VALUES(supplier_name);

-- 4. 商品
INSERT INTO product (id, product_code, product_name, category_id, supplier_id, unit, price, cost_price, status) VALUES
(1, 'P001', '可口可乐', 12, 1, '瓶', 3.50, 2.50, 1),
(2, 'P002', '康师傅方便面', 11, 1, '袋', 4.50, 3.00, 1),
(3, 'P003', '洗衣液', 21, 2, '瓶', 25.00, 18.00, 1),
(4, 'P004', '苹果', 3, 3, '斤', 8.00, 5.00, 1),
(5, 'P005', '雪碧', 12, 1, '瓶', 3.50, 2.50, 1),
(6, 'P006', '薯片', 11, 1, '袋', 6.50, 4.00, 1),
(7, 'P007', '洗发水', 21, 2, '瓶', 35.00, 25.00, 1),
(8, 'P008', '香蕉', 3, 3, '斤', 5.00, 3.00, 1)
ON DUPLICATE KEY UPDATE 
    product_name = VALUES(product_name),
    price = VALUES(price);

-- 5. 库存
INSERT INTO inventory (product_id, quantity, warning_quantity) VALUES
(1, 100, 20),
(2, 50, 10),
(3, 30, 5),
(4, 80, 15),
(5, 120, 20),
(6, 60, 15),
(7, 40, 10),
(8, 90, 20)
ON DUPLICATE KEY UPDATE 
    quantity = VALUES(quantity);

-- 验证数据
SELECT '=== 统计信息 ===' as info;
SELECT COUNT(*) as category_count FROM category;
SELECT COUNT(*) as supplier_count FROM supplier;
SELECT COUNT(*) as product_count FROM product;
SELECT COUNT(*) as inventory_count FROM inventory;

SELECT '=== 商品列表（前5条）===' as info;
SELECT p.id, p.product_name, c.category_name, s.supplier_name, p.price, i.quantity
FROM product p
LEFT JOIN category c ON p.category_id = c.id
LEFT JOIN supplier s ON p.supplier_id = s.id
LEFT JOIN inventory i ON p.id = i.product_id
LIMIT 5;


