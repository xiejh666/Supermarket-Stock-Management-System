-- ============================================
-- 采购管理模块数据库字段更新脚本
-- ============================================
-- 说明：
-- 1. 移除purchase_date字段（改用create_time作为采购时间）
-- 2. 添加inbound_time字段（入库时间）
-- 3. 执行此脚本前请确保已备份数据库
-- 4. 执行后需要重启后端服务
-- ============================================

USE `supermarket`;

-- 检查purchase_date字段是否存在
SELECT COUNT(*) as field_exists
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'purchase_order' 
  AND COLUMN_NAME = 'purchase_date';

-- 如果purchase_date字段存在，则删除它
ALTER TABLE `purchase_order` 
DROP COLUMN IF EXISTS `purchase_date`;

-- 检查inbound_time字段是否已存在
SELECT COUNT(*) as field_exists
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'purchase_order' 
  AND COLUMN_NAME = 'inbound_time';

-- 添加inbound_time字段（入库时间）
ALTER TABLE `purchase_order` 
ADD COLUMN IF NOT EXISTS `inbound_time` DATETIME DEFAULT NULL COMMENT '入库时间' 
AFTER `audit_remark`;

-- 验证字段是否添加成功
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'purchase_order' 
  AND COLUMN_NAME IN ('inbound_time', 'create_time');

-- 查看表结构
DESC `purchase_order`;

-- 验证数据
SELECT id, order_no, status, create_time as purchase_time, inbound_time, audit_time
FROM `purchase_order` 
ORDER BY id DESC
LIMIT 10;

-- ============================================
-- 状态说明：
-- 0 - 待审核（新建订单）
-- 1 - 待入库（审核通过）
-- 2 - 已拒绝（审核拒绝）
-- 3 - 已入库（确认入库）
-- ============================================

-- 时间字段说明：
-- create_time - 采购时间（创建订单的时间）
-- audit_time - 审核时间（管理员审核的时间）
-- inbound_time - 入库时间（确认入库的时间）
-- ============================================
