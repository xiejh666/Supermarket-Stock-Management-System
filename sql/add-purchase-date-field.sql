-- ============================================
-- 采购管理模块数据库迁移脚本
-- ============================================
-- 说明：
-- 1. 此脚本为采购订单表添加采购日期字段
-- 2. purchase_date字段用于记录采购日期
-- 3. 执行此脚本前请确保已备份数据库
-- 4. 执行后需要重启后端服务
-- ============================================

-- 检查字段是否已存在
SELECT COUNT(*) as field_exists
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'purchase_order' 
  AND COLUMN_NAME = 'purchase_date';

-- 如果上面查询结果为0，则执行下面的SQL
-- 为采购订单表添加采购日期字段
ALTER TABLE `purchase_order` 
ADD COLUMN `purchase_date` DATE DEFAULT NULL COMMENT '采购日期' 
AFTER `total_amount`;

-- 验证字段是否添加成功
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'purchase_order' 
  AND COLUMN_NAME = 'purchase_date';

-- 为现有的采购订单设置默认采购日期（使用创建时间的日期部分）
UPDATE `purchase_order` 
SET `purchase_date` = DATE(create_time) 
WHERE `purchase_date` IS NULL;

-- 验证数据更新
SELECT id, order_no, purchase_date, create_time 
FROM `purchase_order` 
LIMIT 10;
