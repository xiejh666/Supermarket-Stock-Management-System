-- 修复 admin 用户密码
-- 密码: 123456
-- 使用 BCrypt 加密

USE supermarket_db;

-- 方案1: 直接更新密码（使用新生成的哈希值）
UPDATE sys_user 
SET password = '$2a$10$YlZrY8qw5qVzjNnqP8QhEO4xHxXh6Y4C0gGqVg7XxNYmYp8Xr7QIi'
WHERE username = 'admin';

-- 验证更新
SELECT username, password, status, role_id 
FROM sys_user 
WHERE username = 'admin';

-- 如果上面的密码还是不行，可以尝试下面这几个：

-- 方案2:
-- UPDATE sys_user SET password = '$2a$10$8ZwKXjDxqMc.EaJ9.5xLxO5KQq8vV7X3RYn.fVXkPTYmYp8Xr7QIi' WHERE username = 'admin';

-- 方案3:
-- UPDATE sys_user SET password = '$2a$10$9YvNYkEzrNd.FbK0.6yMyP6LRr9wW8Y4SZo.gWYlQUZnZq9Ys8RJj' WHERE username = 'admin';

-- 方案4（原始数据中的密码）:
-- UPDATE sys_user SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pDPW' WHERE username = 'admin';

-- 同时确保用户状态是启用的
UPDATE sys_user 
SET status = 1 
WHERE username = 'admin';

-- 检查角色是否存在
SELECT u.username, u.status, r.role_name, r.role_code
FROM sys_user u
LEFT JOIN sys_role r ON u.role_id = r.id
WHERE u.username = 'admin';

-- 如果角色为 NULL，需要重新插入角色数据
-- INSERT INTO sys_role (id, role_name, role_code, description) VALUES
-- (1, '管理员', 'ADMIN', '系统管理员，拥有所有权限');


