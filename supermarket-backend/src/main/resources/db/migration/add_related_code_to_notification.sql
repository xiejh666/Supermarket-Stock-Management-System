-- 为系统通知表添加 related_code 字段，用于存储商品编码等关键标识
ALTER TABLE system_notification ADD COLUMN related_code VARCHAR(100) COMMENT '关联编码（如商品编码、订单号等）' AFTER business_type;

-- 为已有的商品通知补充商品编码（从product表的product_code字段获取）
UPDATE system_notification sn
INNER JOIN product p ON sn.business_id = p.id
SET sn.related_code = p.product_code
WHERE sn.business_type = 'PRODUCT' AND sn.related_code IS NULL;
