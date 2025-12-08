-- 为 sys_user 表添加 avatar 字段
ALTER TABLE sys_user ADD COLUMN avatar VARCHAR(500) COMMENT '用户头像URL' AFTER email;

-- 为 product 表的 image 字段添加注释（如果还没有）
ALTER TABLE product MODIFY COLUMN image VARCHAR(500) COMMENT '商品图片URL';
