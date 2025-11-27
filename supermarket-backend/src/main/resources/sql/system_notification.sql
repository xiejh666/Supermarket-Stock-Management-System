-- 系统通知表
CREATE TABLE IF NOT EXISTS `system_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(200) NOT NULL COMMENT '通知标题',
  `content` text NOT NULL COMMENT '通知内容',
  `type` varchar(50) NOT NULL COMMENT '通知类型：PURCHASE_AUDIT-采购审核，SALE_PAYMENT-销售支付，SYSTEM-系统通知',
  `receiver_id` bigint DEFAULT NULL COMMENT '接收用户ID（为空表示发给所有管理员）',
  `sender_id` bigint NOT NULL COMMENT '发送用户ID',
  `business_id` bigint DEFAULT NULL COMMENT '关联业务ID（如采购订单ID、销售订单ID）',
  `business_type` varchar(50) DEFAULT NULL COMMENT '业务类型：PURCHASE_ORDER-采购订单，SALE_ORDER-销售订单',
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '是否已读：0-未读，1-已读',
  `priority` tinyint NOT NULL DEFAULT '2' COMMENT '优先级：1-低，2-中，3-高',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_business` (`business_id`, `business_type`),
  KEY `idx_type_read` (`type`, `is_read`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知表';
