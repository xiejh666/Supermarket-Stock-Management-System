# 图片上传功能部署指南 🚀

## 📋 部署前检查清单

在开始部署之前，请确保以下条件都已满足：

- ✅ 阿里云 OSS 账号已创建
- ✅ OSS Bucket `supermarket-system` 已创建
- ✅ OSS Bucket 设置为**公共读**权限
- ✅ MySQL 数据库正在运行
- ✅ Redis 服务正在运行
- ✅ Node.js 和 npm 已安装
- ✅ Java 17 和 Maven 已安装

---

## 🗄️ 第一步：执行数据库脚本

### 1. 连接到 MySQL 数据库

```bash
mysql -u root -p
```

### 2. 选择数据库

```sql
USE supermarket_db;
```

### 3. 添加用户头像字段

```sql
-- 为 sys_user 表添加 avatar 字段
ALTER TABLE sys_user ADD COLUMN avatar VARCHAR(500) COMMENT '用户头像URL' AFTER email;

-- 验证字段是否添加成功
DESC sys_user;
```

### 4. 验证商品图片字段

```sql
-- 确认 product 表已有 image 字段
DESC product;

-- 如果没有，执行以下语句
-- ALTER TABLE product ADD COLUMN image VARCHAR(500) COMMENT '商品图片URL';
```

---

## ☁️ 第二步：配置阿里云 OSS

### 1. 登录阿里云 OSS 控制台

访问：https://oss.console.aliyun.com/

### 2. 确认 Bucket 配置

- **Bucket 名称**：`supermarket-system`
- **地域**：华东1（杭州）
- **读写权限**：**公共读**（重要！）

### 3. 设置 Bucket 为公共读

1. 进入 Bucket 管理页面
2. 点击"权限管理" → "Bucket 授权策略"
3. 设置为：**公共读**
4. 保存设置

### 4. 测试 OSS 访问

上传一张测试图片，获取 URL，在浏览器中访问，确保可以正常显示。

---

## 🔧 第三步：配置后端

### 1. 确认 application.yml 配置

文件路径：`supermarket-backend/src/main/resources/application.yml`

```yaml
# 阿里云OSS配置
oss:
  endpoint: oss-cn-hangzhou.aliyuncs.com
  access-key-id: LTAI5tL3mDXQjcBWs9qTGWoz
  access-key-secret: bHIOOmCsuCEe6ixyb65HrZmFTtUpkk
  bucket-name: supermarket-system
  url-prefix: https://supermarket-system.oss-cn-hangzhou.aliyuncs.com/
```

**注意**：请确保 `url-prefix` 与您的 Bucket 名称和地域一致！

### 2. 重新编译后端

```bash
cd supermarket-backend
mvn clean package -DskipTests
```

### 3. 启动后端服务

**方式一：在 IDEA 中运行**
- 右键 `SupermarketBackendApplication.java`
- 点击 "Run"

**方式二：使用命令行**
```bash
java -jar target/supermarket-backend.jar
```

### 4. 验证后端启动

访问：http://8.136.43.180:8080/api/doc.html

确认 Swagger 文档可以正常访问，并且可以看到以下接口：
- `/upload/avatar` - 上传用户头像
- `/upload/product` - 上传商品图片
- `/user/profile/avatar` - 更新用户头像

---

## 🎨 第四步：配置前端

### 1. 安装依赖（如果还没有）

```bash
cd supermarket-frontend
npm install
```

### 2. 启动前端服务

```bash
npm run dev
```

### 3. 验证前端启动

浏览器会自动打开：http://8.136.43.180:5173

---

## 🧪 第五步：功能测试

### 测试 1：用户头像上传

1. **登录系统**
   ```
   用户名：admin
   密码：123456
   ```

2. **查看右上角**
   - 应该显示默认头像

3. **进入个人中心**
   - 点击右上角头像
   - 选择"个人中心"

4. **上传头像**
   - 点击"更换头像"按钮
   - 选择一张图片（小于 2MB）
   - 点击"确定"

5. **验证结果**
   - ✅ 右上角头像立即更新
   - ✅ 刷新页面，头像仍然存在
   - ✅ 重新登录，头像仍然存在

### 测试 2：商品图片上传

1. **进入商品管理**
   - 点击侧边栏"商品管理"

2. **新增商品**
   - 点击"新增商品"按钮
   - 填写商品信息：
     ```
     商品名称：测试商品
     商品编码：TEST001
     商品分类：选择任意分类
     规格：500ml
     单位：瓶
     成本价：10.00
     零售价：15.00
     预警库存：10
     ```
   - 点击图片上传区域
   - 选择商品图片（小于 5MB）
   - 等待上传完成
   - 点击"确定"

3. **验证结果**
   - ✅ 商品列表中显示商品图片
   - ✅ 点击"查看"按钮
   - ✅ 详情对话框中显示大图
   - ✅ 点击图片可以预览

4. **编辑商品**
   - 点击"编辑"按钮
   - ✅ 可以看到之前上传的图片
   - ✅ 可以重新上传替换图片

### 测试 3：图片持久化

1. **刷新页面**
   - 按 F5 刷新
   - ✅ 头像仍然显示
   - ✅ 商品图片仍然显示

2. **重新登录**
   - 退出登录
   - 重新登录
   - ✅ 头像仍然显示

3. **重启服务**
   - 重启后端和前端服务
   - 重新登录
   - ✅ 所有图片仍然正常显示

---

## 🐛 常见问题排查

### 问题 1：图片上传后无法显示

**原因**：OSS Bucket 权限设置不正确

**解决方案**：
1. 登录阿里云 OSS 控制台
2. 选择 Bucket：`supermarket-system`
3. 权限管理 → Bucket 授权策略
4. 设置为：**公共读**

### 问题 2：上传图片时提示 401 错误

**原因**：AccessKey 配置错误或已过期

**解决方案**：
1. 检查 `application.yml` 中的 AccessKey 配置
2. 确认 AccessKey 是否正确
3. 确认 AccessKey 是否有 OSS 操作权限

### 问题 3：上传图片时提示跨域错误

**原因**：OSS 跨域配置未设置

**解决方案**：
1. 登录阿里云 OSS 控制台
2. 选择 Bucket：`supermarket-system`
3. 数据安全 → 跨域设置
4. 添加跨域规则：
   ```
   来源：*
   允许 Methods：GET, POST, PUT, DELETE, HEAD
   允许 Headers：*
   暴露 Headers：ETag
   ```

### 问题 4：数据库字段不存在

**错误信息**：`Unknown column 'avatar' in 'field list'`

**解决方案**：
```sql
-- 执行数据库脚本
ALTER TABLE sys_user ADD COLUMN avatar VARCHAR(500) COMMENT '用户头像URL' AFTER email;
```

### 问题 5：图片上传成功但不显示

**原因**：图片 URL 格式不正确

**解决方案**：
1. 检查 `application.yml` 中的 `url-prefix` 配置
2. 确保格式为：`https://bucket-name.oss-region.aliyuncs.com/`
3. 注意结尾的 `/` 不能省略

---

## 📊 性能优化建议

### 1. 图片压缩

建议在上传前对图片进行压缩，减少存储空间和加载时间。

**前端压缩方案**：
- 使用 `compressorjs` 库
- 在 `beforeUpload` 中进行压缩

### 2. CDN 加速

**阿里云 CDN 配置**：
1. 开通阿里云 CDN 服务
2. 添加加速域名
3. 源站设置为 OSS Bucket
4. 更新 `url-prefix` 为 CDN 域名

### 3. 图片懒加载

前端已实现图片懒加载，无需额外配置。

### 4. 缓存策略

**OSS 缓存设置**：
1. 进入 OSS 控制台
2. 基础设置 → HTTP 头
3. 设置 Cache-Control：`max-age=31536000`

---

## 🔒 安全建议

### 1. AccessKey 安全

- ❌ 不要将 AccessKey 提交到 Git 仓库
- ✅ 使用环境变量或配置中心管理
- ✅ 定期更换 AccessKey

### 2. 文件类型限制

- ✅ 只允许上传图片文件
- ✅ 验证文件扩展名和 MIME 类型
- ✅ 限制文件大小

### 3. 访问控制

- ✅ 上传接口需要登录认证
- ✅ 使用 JWT Token 验证
- ✅ 限制上传频率（防止恶意上传）

---

## 📝 部署检查清单

部署完成后，请逐项检查以下内容：

- [ ] 数据库脚本已执行
- [ ] OSS Bucket 已创建并设置为公共读
- [ ] application.yml 配置正确
- [ ] 后端服务启动成功
- [ ] 前端服务启动成功
- [ ] 可以正常登录系统
- [ ] 右上角显示头像
- [ ] 可以上传用户头像
- [ ] 可以上传商品图片
- [ ] 图片可以正常显示
- [ ] 刷新页面图片仍然存在
- [ ] 重新登录图片仍然存在

---

## 🎉 部署完成！

恭喜！您已经成功部署了图片上传功能！

现在您可以：
- ✅ 上传和更换用户头像
- ✅ 上传商品图片
- ✅ 在列表和详情中查看图片
- ✅ 所有图片都存储在阿里云 OSS 上

如果遇到任何问题，请参考"常见问题排查"部分。

---

## 📞 技术支持

如果您在部署过程中遇到问题，可以：

1. 查看后端日志：`logs/spring.log`
2. 查看浏览器控制台错误信息
3. 检查网络请求（F12 → Network）
4. 参考完整文档：`IMAGE_UPLOAD_COMPLETE.md`

---

**祝您使用愉快！** 🎊
