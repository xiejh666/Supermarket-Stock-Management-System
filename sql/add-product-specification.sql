-- ============================================
-- 商品管理模块数据库迁移脚本
-- ============================================
-- 说明：
-- 1. 此脚本为商品表添加规格字段
-- 2. specification字段用于存储商品规格信息，如"500ml"、"1kg"等
-- 3. 执行此脚本前请确保已备份数据库
-- 4. 执行后需要重启后端服务
-- ============================================

-- 为商品表添加规格字段
ALTER TABLE `product` ADD COLUMN `specification` VARCHAR(50) DEFAULT NULL COMMENT '规格' AFTER `unit`;

-- 验证字段是否添加成功
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'product' 
  AND COLUMN_NAME = 'specification';
