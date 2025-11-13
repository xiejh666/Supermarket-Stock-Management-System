-- 测试数据生成脚本
-- 用于Dashboard图表展示

-- 清理现有测试数据（可选）
-- DELETE FROM sale_order WHERE order_no LIKE 'TEST_%';
-- DELETE FROM purchase_order WHERE order_no LIKE 'TEST_%';

-- 1. 首先生成客户数据（确保外键约束）
INSERT IGNORE INTO customer (id, customer_name, phone, address, create_time, update_time) VALUES
(1, '张先生', '13800138001', '北京市朝阳区建国路88号', NOW(), NOW()),
(2, '李女士', '13800138002', '上海市浦东新区陆家嘴金融中心', NOW(), NOW()),
(3, '王总', '13800138003', '广州市天河区珠江新城', NOW(), NOW()),
(4, '刘经理', '13800138004', '深圳市南山区科技园', NOW(), NOW()),
(5, '陈女士', '13800138005', '杭州市西湖区文三路', NOW(), NOW()),
(6, '赵先生', '13800138006', '成都市锦江区春熙路', NOW(), NOW()),
(7, '孙女士', '13800138007', '武汉市江汉区中山大道', NOW(), NOW()),
(8, '周总', '13800138008', '南京市鼓楼区中山路', NOW(), NOW()),
(9, '吴经理', '13800138009', '西安市雁塔区高新区', NOW(), NOW()),
(10, '郑女士', '13800138010', '重庆市渝中区解放碑', NOW(), NOW());

-- 2. 生成销售订单数据（最近7天）
INSERT INTO sale_order (order_no, customer_id, total_amount, status, cashier_id, create_time, update_time) VALUES
-- 7天前
('TEST_SALE_001', 1, 1250.00, 1, 3, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
('TEST_SALE_002', 2, 890.50, 1, 3, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
('TEST_SALE_003', 3, 2100.00, 1, 3, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),

-- 6天前
('TEST_SALE_004', 1, 1580.00, 1, 3, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
('TEST_SALE_005', 2, 920.00, 1, 3, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
('TEST_SALE_006', 3, 1750.50, 1, 3, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
('TEST_SALE_007', 4, 680.00, 1, 3, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),

-- 5天前
('TEST_SALE_008', 1, 2200.00, 1, 3, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
('TEST_SALE_009', 2, 1150.00, 1, 3, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
('TEST_SALE_010', 3, 1980.50, 1, 3, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),

-- 4天前
('TEST_SALE_011', 1, 3200.00, 1, 3, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
('TEST_SALE_012', 2, 1850.00, 1, 3, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
('TEST_SALE_013', 3, 2650.50, 1, 3, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
('TEST_SALE_014', 4, 1200.00, 1, 3, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
('TEST_SALE_015', 5, 980.00, 1, 3, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- 3天前
('TEST_SALE_016', 1, 2800.00, 1, 3, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('TEST_SALE_017', 2, 1950.00, 1, 3, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('TEST_SALE_018', 3, 3150.50, 1, 3, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('TEST_SALE_019', 4, 1680.00, 1, 3, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- 2天前
('TEST_SALE_020', 1, 4200.00, 1, 3, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
('TEST_SALE_021', 2, 2850.00, 1, 3, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
('TEST_SALE_022', 3, 3650.50, 1, 3, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
('TEST_SALE_023', 4, 2200.00, 1, 3, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
('TEST_SALE_024', 5, 1980.00, 1, 3, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
('TEST_SALE_025', 6, 1750.00, 1, 3, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- 今天
('TEST_SALE_026', 1, 5200.00, 1, 3, NOW(), NOW()),
('TEST_SALE_027', 2, 3850.00, 1, 3, NOW(), NOW()),
('TEST_SALE_028', 3, 4650.50, 1, 3, NOW(), NOW()),
('TEST_SALE_029', 4, 2800.00, 1, 3, NOW(), NOW()),
('TEST_SALE_030', 5, 2180.00, 1, 3, NOW(), NOW()),
('TEST_SALE_031', 6, 1950.00, 1, 3, NOW(), NOW()),
('TEST_SALE_032', 7, 1680.00, 1, 3, NOW(), NOW());

-- 3. 生成采购订单数据
INSERT INTO purchase_order (order_no, supplier_id, total_amount, status, applicant_id, create_time, update_time) VALUES
-- 本月已审核的采购订单
('TEST_PUR_001', 1, 15000.00, 1, 1, DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY)),
('TEST_PUR_002', 2, 12500.00, 1, 1, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY)),
('TEST_PUR_003', 3, 18000.00, 1, 1, DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY)),
('TEST_PUR_004', 1, 22000.00, 1, 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY)),
('TEST_PUR_005', 2, 16500.00, 1, 1, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),

-- 待审核的采购订单
('TEST_PUR_006', 1, 8500.00, 0, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('TEST_PUR_007', 3, 12000.00, 0, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
('TEST_PUR_008', 2, 9500.00, 0, 1, NOW(), NOW());

-- 4. 更新库存数据，创建一些低库存预警
UPDATE inventory SET quantity = 5, warning_quantity = 20 WHERE product_id = 1;
UPDATE inventory SET quantity = 3, warning_quantity = 15 WHERE product_id = 2;
UPDATE inventory SET quantity = 8, warning_quantity = 25 WHERE product_id = 3;
UPDATE inventory SET quantity = 12, warning_quantity = 30 WHERE product_id = 4;
UPDATE inventory SET quantity = 2, warning_quantity = 10 WHERE product_id = 5;

-- 5. 确保有营业员用户数据（ID=3）
INSERT IGNORE INTO sys_user (id, username, password, real_name, phone, email, status, role_id, create_time, update_time) VALUES
(3, 'cashier', '$2a$10$7JB720yubVSOfvVwaMV/OeImqrhWJTQvOzjHHxLjNWJWpVcXZPdSa', '营业员', '13800001003', 'cashier@test.com', 1, 4, NOW(), NOW());

-- 6. 确保有商品分类数据
INSERT IGNORE INTO category (category_name, parent_id, create_time, update_time) VALUES
('食品饮料', 0, NOW(), NOW()),
('日用百货', 0, NOW(), NOW()),
('电子产品', 0, NOW(), NOW()),
('服装鞋帽', 0, NOW(), NOW()),
('家居用品', 0, NOW(), NOW());

-- 7. 确保有商品数据
INSERT IGNORE INTO product (product_code, product_name, category_id, price, image, description, status, create_time, update_time) VALUES
('P001', '可口可乐 330ml', 1, 3.50, '', '经典可乐', 1, NOW(), NOW()),
('P002', '康师傅方便面', 1, 4.50, '', '红烧牛肉面', 1, NOW(), NOW()),
('P003', '洗衣粉 2kg', 2, 15.80, '', '强力去污', 1, NOW(), NOW()),
('P004', '苹果手机充电器', 3, 89.00, '', '原装充电器', 1, NOW(), NOW()),
('P005', '纯棉T恤', 4, 59.90, '', '100%纯棉', 1, NOW(), NOW()),
('P006', '不锈钢水杯', 5, 25.00, '', '保温杯', 1, NOW(), NOW()),
('P007', '薯片 大包装', 1, 8.50, '', '香脆薯片', 1, NOW(), NOW()),
('P008', '洗发水 500ml', 2, 28.90, '', '柔顺洗发', 1, NOW(), NOW()),
('P009', '蓝牙耳机', 3, 199.00, '', '无线蓝牙', 1, NOW(), NOW()),
('P010', '运动鞋', 4, 299.00, '', '透气运动鞋', 1, NOW(), NOW());

-- 8. 确保有供应商数据
INSERT IGNORE INTO supplier (supplier_name, contact_person, phone, address, status, create_time, update_time) VALUES
('可口可乐公司', '张经理', '400-800-8888', '上海市浦东新区', 1, NOW(), NOW()),
('康师傅集团', '李经理', '400-700-7777', '天津市滨海新区', 1, NOW(), NOW()),
('宝洁公司', '王经理', '400-600-6666', '广州市天河区', 1, NOW(), NOW()),
('苹果授权经销商', '赵经理', '400-500-5555', '深圳市南山区', 1, NOW(), NOW()),
('纺织品供应商', '刘经理', '400-400-4444', '江苏省南通市', 1, NOW(), NOW());

-- 9. 初始化库存（如果不存在）
INSERT IGNORE INTO inventory (product_id, quantity, warning_quantity, create_time, update_time) VALUES
(1, 5, 20, NOW(), NOW()),
(2, 3, 15, NOW(), NOW()),
(3, 8, 25, NOW(), NOW()),
(4, 12, 30, NOW(), NOW()),
(5, 2, 10, NOW(), NOW()),
(6, 45, 20, NOW(), NOW()),
(7, 28, 15, NOW(), NOW()),
(8, 35, 25, NOW(), NOW()),
(9, 18, 10, NOW(), NOW()),
(10, 22, 15, NOW(), NOW());

-- 查询验证数据
SELECT '=== 销售订单统计 ===' as info;
SELECT 
    DATE(create_time) as date,
    COUNT(*) as order_count,
    SUM(total_amount) as total_sales
FROM sale_order 
WHERE create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY DATE(create_time)
ORDER BY date;

SELECT '=== 采购订单统计 ===' as info;
SELECT 
    status,
    COUNT(*) as order_count,
    SUM(total_amount) as total_amount
FROM purchase_order 
GROUP BY status;

SELECT '=== 库存预警统计 ===' as info;
SELECT 
    p.product_name,
    i.quantity as current_stock,
    i.warning_quantity,
    CASE 
        WHEN i.quantity <= i.warning_quantity * 0.2 THEN '严重'
        WHEN i.quantity <= i.warning_quantity * 0.5 THEN '中等'
        ELSE '轻微'
    END as warning_level
FROM inventory i
JOIN product p ON i.product_id = p.id
WHERE i.quantity <= i.warning_quantity
ORDER BY (i.quantity / i.warning_quantity) ASC;
