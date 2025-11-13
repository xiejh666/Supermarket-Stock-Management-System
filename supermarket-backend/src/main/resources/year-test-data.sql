-- 生成一年的销售测试数据
-- 从今年1月1日到今天，每天随机生成1-10个订单，呈缓慢增长趋势

-- 清理现有测试数据（可选）
-- DELETE FROM sale_order WHERE order_no LIKE 'YEAR_%';

-- 生成一年的销售订单数据
-- 使用存储过程批量生成数据
DELIMITER $$

CREATE PROCEDURE IF NOT EXISTS GenerateYearSalesData()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE loop_date DATE DEFAULT DATE(CONCAT(YEAR(NOW()), '-01-01'));
    DECLARE end_date DATE DEFAULT CURDATE();
    DECLARE order_count INT;
    DECLARE order_index INT;
    DECLARE order_no VARCHAR(50);
    DECLARE customer_id_val INT;
    DECLARE amount_val DECIMAL(10,2);
    DECLARE base_amount DECIMAL(10,2);
    DECLARE growth_factor DECIMAL(5,4);
    DECLARE random_factor DECIMAL(5,4);
    DECLARE days_passed INT;
    
    -- 删除现有的年度测试数据
    DELETE FROM sale_order WHERE order_no LIKE 'YEAR_%';
    
    WHILE loop_date <= end_date DO
        -- 计算距离年初的天数，用于增长趋势
        SET days_passed = DATEDIFF(loop_date, DATE(CONCAT(YEAR(NOW()), '-01-01')));
        
        -- 计算增长因子（年初1.0，年末1.5，呈缓慢增长）
        SET growth_factor = 1.0 + (days_passed / 365.0) * 0.5;
        
        -- 每天生成1-8个订单，周末可能更多
        IF DAYOFWEEK(loop_date) IN (1, 7) THEN -- 周末
            SET order_count = FLOOR(RAND() * 6) + 3; -- 3-8个订单
        ELSE -- 工作日
            SET order_count = FLOOR(RAND() * 5) + 1; -- 1-5个订单
        END IF;
        
        -- 应用增长趋势
        SET order_count = CEIL(order_count * growth_factor);
        
        SET order_index = 1;
        WHILE order_index <= order_count DO
            -- 生成订单编号
            SET order_no = CONCAT('YEAR_', DATE_FORMAT(loop_date, '%Y%m%d'), '_', LPAD(order_index, 3, '0'));
            
            -- 随机选择客户（1-10）
            SET customer_id_val = FLOOR(RAND() * 10) + 1;
            
            -- 基础金额随月份增长（500-2000基础，加上增长趋势）
            SET base_amount = 500 + FLOOR(RAND() * 1500);
            SET random_factor = 0.8 + (RAND() * 0.4); -- 0.8-1.2的随机因子
            SET amount_val = base_amount * growth_factor * random_factor;
            
            -- 插入订单数据
            INSERT INTO sale_order (order_no, customer_id, total_amount, status, cashier_id, create_time, update_time)
            VALUES (
                order_no,
                customer_id_val,
                amount_val,
                1, -- 已完成状态
                3, -- 营业员ID
                CONCAT(loop_date, ' ', TIME(ADDTIME('08:00:00', SEC_TO_TIME(FLOOR(RAND() * 43200))))), -- 8:00-20:00随机时间
                CONCAT(loop_date, ' ', TIME(ADDTIME('08:00:00', SEC_TO_TIME(FLOOR(RAND() * 43200)))))
            );
            
            SET order_index = order_index + 1;
        END WHILE;
        
        SET loop_date = DATE_ADD(loop_date, INTERVAL 1 DAY);
    END WHILE;
    
    -- 显示生成结果
    SELECT CONCAT('成功生成 ', COUNT(*), ' 条年度销售订单数据') as result
    FROM sale_order WHERE order_no LIKE 'YEAR_%';
    
END$$

DELIMITER ;

-- 执行存储过程
CALL GenerateYearSalesData();

-- 删除存储过程
DROP PROCEDURE IF EXISTS GenerateYearSalesData;

-- 验证数据生成结果
SELECT '=== 年度销售数据统计 ===' as info;

-- 按月统计
SELECT 
    DATE_FORMAT(create_time, '%Y-%m') as month,
    COUNT(*) as order_count,
    ROUND(SUM(total_amount), 2) as total_sales,
    ROUND(AVG(total_amount), 2) as avg_order_amount
FROM sale_order 
WHERE order_no LIKE 'YEAR_%'
GROUP BY DATE_FORMAT(create_time, '%Y-%m')
ORDER BY month;

-- 按周统计最近4周
SELECT 
    CONCAT(DATE_FORMAT(DATE_SUB(MIN(create_time), INTERVAL WEEKDAY(MIN(create_time)) DAY), '%m-%d'), 
           ' 至 ', 
           DATE_FORMAT(DATE_ADD(DATE_SUB(MIN(create_time), INTERVAL WEEKDAY(MIN(create_time)) DAY), INTERVAL 6 DAY), '%m-%d')) as week_range,
    COUNT(*) as order_count,
    ROUND(SUM(total_amount), 2) as total_sales
FROM sale_order 
WHERE order_no LIKE 'YEAR_%' 
  AND create_time >= DATE_SUB(CURDATE(), INTERVAL 4 WEEK)
GROUP BY YEAR(create_time), WEEK(create_time)
ORDER BY MIN(create_time);

-- 按日统计最近7天
SELECT 
    DATE(create_time) as date,
    DAYNAME(DATE(create_time)) as day_name,
    COUNT(*) as order_count,
    ROUND(SUM(total_amount), 2) as total_sales
FROM sale_order 
WHERE order_no LIKE 'YEAR_%' 
  AND create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
GROUP BY DATE(create_time)
ORDER BY date;
