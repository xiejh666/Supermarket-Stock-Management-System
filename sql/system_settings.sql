-- 系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` TEXT COMMENT '配置值',
  `config_desc` VARCHAR(200) DEFAULT NULL COMMENT '配置描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 用户配置表
CREATE TABLE IF NOT EXISTS `user_config` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` VARCHAR(500) COMMENT '配置值',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_config` (`user_id`, `config_key`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户配置表';

-- 插入初始系统配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_desc`) VALUES
('system.name', '超市进销存管理系统', '系统名称'),
('system.version', 'v1.0.0', '系统版本'),
('system.description', '专业的超市进销存管理解决方案，提供商品、采购、销售、库存全流程管理', '系统描述'),
('security.password_expire_days', '90', '密码过期天数'),
('security.login_fail_times', '5', '登录失败锁定次数'),
('security.session_timeout', '30', '会话超时时间(分钟)'),
('security.strong_password', 'true', '是否启用强密码策略');

-- 插入默认用户配置（给管理员）
INSERT INTO `user_config` (`user_id`, `config_key`, `config_value`) VALUES
(1, 'notification.inventory_warning', 'true'),
(1, 'notification.order_audit', 'true'),
(1, 'notification.system_notice', 'true');


