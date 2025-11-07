-- 消息通知表
CREATE TABLE IF NOT EXISTS `message_notification` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `user_id` BIGINT(20) DEFAULT NULL COMMENT '接收用户ID（NULL表示所有人可见）',
  `role_code` VARCHAR(50) DEFAULT NULL COMMENT '接收角色编码（NULL表示所有角色可见）',
  `type` VARCHAR(20) NOT NULL COMMENT '消息类型：success/warning/info/error',
  `category` VARCHAR(50) NOT NULL COMMENT '消息分类：system/purchase/sale/inventory/user',
  `title` VARCHAR(200) NOT NULL COMMENT '消息标题',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `link_type` VARCHAR(50) DEFAULT NULL COMMENT '跳转类型：purchase/sale/inventory/product/supplier',
  `link_id` BIGINT(20) DEFAULT NULL COMMENT '关联业务ID',
  `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_code` (`role_code`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- 插入测试消息数据
INSERT INTO `message_notification` (`user_id`, `role_code`, `type`, `category`, `title`, `content`, `link_type`, `link_id`, `is_read`, `create_time`) VALUES
-- 管理员消息
(1, 'ADMIN', 'warning', 'purchase', '采购审核提醒', '采购订单 PO202411070001 待审核，请及时处理', 'purchase', 1, 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(1, 'ADMIN', 'error', 'inventory', '库存预警', '商品"可口可乐"库存仅剩5件，低于预警值10件', 'inventory', 1, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(NULL, NULL, 'success', 'system', '系统通知', '欢迎使用超市进销存管理系统！', NULL, NULL, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR)),

-- 采购员消息
(2, 'PURCHASER', 'success', 'purchase', '采购订单已通过', '您的采购订单 PO202411070002 已审核通过，可以进行入库操作', 'purchase', 2, 0, DATE_SUB(NOW(), INTERVAL 45 MINUTE)),
(2, 'PURCHASER', 'info', 'purchase', '采购入库完成', '采购订单 PO202411070003 已成功入库，库存已更新', 'purchase', 3, 1, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(NULL, 'PURCHASER', 'warning', 'inventory', '库存预警提醒', '有5件商品库存低于预警值，请及时采购补货', 'inventory', NULL, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR)),

-- 营业员消息
(3, 'CASHIER', 'success', 'sale', '销售订单已完成', '销售订单 SO202411070001 已成功出库，销售金额：¥158.00', 'sale', 1, 0, DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
(3, 'CASHIER', 'warning', 'sale', '商品库存不足', '商品"雪碧"库存不足，无法满足销售需求', 'inventory', 2, 0, DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
(NULL, 'CASHIER', 'info', 'system', '销售统计更新', '今日销售额已达¥2,580.00，完成目标的86%', NULL, NULL, 1, DATE_SUB(NOW(), INTERVAL 4 HOUR));

-- 添加更多实时消息（用于演示不同时间）
INSERT INTO `message_notification` (`role_code`, `type`, `category`, `title`, `content`, `link_type`, `link_id`, `is_read`, `create_time`) VALUES
('ADMIN', 'info', 'user', '新用户注册', '新用户"李四"已注册，待审核', 'user', 4, 0, DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
('PURCHASER', 'warning', 'purchase', '采购订单超时', '采购订单 PO202411060005 已超过预期到货时间', 'purchase', 5, 0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
('ADMIN', 'success', 'system', '数据备份完成', '系统数据已自动备份成功', NULL, NULL, 1, DATE_SUB(NOW(), INTERVAL 1 DAY));

SELECT COUNT(*) FROM message_notification;
-- 应该返回 12