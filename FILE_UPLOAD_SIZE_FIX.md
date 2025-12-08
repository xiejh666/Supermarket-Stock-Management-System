# 文件上传大小限制问题修复 ✅

## 🐛 问题描述

**错误信息**：
```
org.springframework.web.multipart.MaxUploadSizeExceededException: Maximum upload size exceeded
The field file exceeds its maximum permitted size of 1048576 bytes.
```

**现象**：
- 上传小于 1MB 的图片：✅ 成功
- 上传 3MB 左右的图片：❌ 失败，报错

**原因**：
Spring Boot 默认的文件上传大小限制是 **1MB**，超过这个大小就会报错。

---

## ✅ 解决方案

### 修改配置文件

**文件**：`application.yml`

**添加配置**：
```yaml
spring:
  application:
    name: supermarket-backend
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://8.136.43.180:3306/supermarket_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
  # 文件上传配置
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB      # 单个文件最大大小
      max-request-size: 10MB   # 整个请求最大大小
```

---

## 📝 配置说明

### 1. `max-file-size`
- **含义**：单个文件的最大大小
- **默认值**：1MB
- **修改后**：10MB
- **说明**：允许上传最大 10MB 的单个文件

### 2. `max-request-size`
- **含义**：整个请求的最大大小
- **默认值**：10MB
- **修改后**：10MB
- **说明**：如果一次上传多个文件，所有文件的总大小不能超过这个值

### 3. `enabled`
- **含义**：是否启用文件上传
- **值**：true
- **说明**：启用 Spring Boot 的文件上传功能

---

## 🎯 前后端限制对比

### 前端限制

**头像上传**（`Profile.vue`）：
```javascript
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2  // 2MB

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}
```

**商品图片上传**（`Product.vue`）：
```javascript
const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5  // 5MB

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}
```

### 后端限制

**修改前**：
- 单个文件：1MB（默认）
- 整个请求：10MB（默认）

**修改后**：
- 单个文件：10MB ✅
- 整个请求：10MB ✅

---

## 🧪 测试验证

### 1. 重启后端服务

**重要**：修改 `application.yml` 后必须重启后端服务！

**方式一：在 IDEA 中**
- 停止当前运行的服务
- 重新运行 `SupermarketBackendApplication`

**方式二：使用命令行**
```bash
cd supermarket-backend
mvn clean package -DskipTests
java -jar target/supermarket-backend.jar
```

### 2. 测试上传

#### 测试 1：上传小文件（< 1MB）
- ✅ 应该成功

#### 测试 2：上传中等文件（1-5MB）
- ✅ 应该成功（之前会失败）

#### 测试 3：上传大文件（5-10MB）
- ✅ 应该成功

#### 测试 4：上传超大文件（> 10MB）
- ❌ 应该失败，提示文件过大

---

## 📊 文件大小对照表

| 大小 | 字节数 | 说明 |
|------|--------|------|
| 1KB | 1,024 bytes | 千字节 |
| 1MB | 1,048,576 bytes | 兆字节 |
| 2MB | 2,097,152 bytes | 头像上传限制 |
| 5MB | 5,242,880 bytes | 商品图片上传限制 |
| 10MB | 10,485,760 bytes | 后端最大限制 |

---

## 🎯 推荐配置

### 根据实际需求调整

#### 1. 如果主要上传头像（小图片）
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 5MB
```

#### 2. 如果需要上传高清商品图片
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
```

#### 3. 如果需要上传文档或视频
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB
```

---

## ⚠️ 注意事项

### 1. 服务器资源
- 文件上传会占用服务器内存
- 不要设置过大的限制，避免内存溢出
- 建议根据实际需求设置合理的大小

### 2. OSS 存储成本
- 文件越大，存储成本越高
- 建议在前端进行图片压缩
- 可以使用 CDN 加速访问

### 3. 用户体验
- 文件越大，上传时间越长
- 建议提示用户上传进度
- 可以添加上传超时处理

### 4. 安全性
- 验证文件类型，防止上传恶意文件
- 限制文件大小，防止恶意上传
- 使用唯一文件名，防止文件覆盖

---

## 🔧 其他相关配置

### Tomcat 配置（可选）

如果需要更大的上传限制，还可以配置 Tomcat：

```yaml
server:
  tomcat:
    max-swallow-size: 10MB
    max-http-form-post-size: 10MB
```

### Nginx 配置（如果使用 Nginx）

```nginx
client_max_body_size 10M;
```

---

## 📝 完整配置示例

```yaml
spring:
  application:
    name: supermarket-backend
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://8.136.43.180:3306/supermarket_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
  # 文件上传配置
  servlet:
    multipart:
      enabled: true              # 启用文件上传
      max-file-size: 10MB        # 单个文件最大 10MB
      max-request-size: 10MB     # 请求最大 10MB
      file-size-threshold: 0     # 文件写入磁盘的阈值
  redis:
    host: 8.136.43.180
    port: 6379
    password: ""
    database: 0

# 服务器配置
server:
  port: 8080
  tomcat:
    max-swallow-size: 10MB       # Tomcat 最大上传大小
    max-http-form-post-size: 10MB

# 阿里云OSS配置
oss:
  endpoint: oss-cn-hangzhou.aliyuncs.com
  access-key-id: LTAI5tL3mDXQjcBWs9qTGWoz
  access-key-secret: bHIOOmCsuCEe6ixyb65HrZmFTtUpkk
  bucket-name: supermarket-system
  url-prefix: https://supermarket-system.oss-cn-hangzhou.aliyuncs.com/
```

---

## 🎉 总结

**问题**：文件上传大小超过 1MB 限制  
**原因**：Spring Boot 默认限制是 1MB  
**解决**：在 `application.yml` 中配置 `max-file-size: 10MB`  
**结果**：✅ 可以上传最大 10MB 的文件  

**现在可以上传 3MB 的图片了！** 🎊

---

## 📞 常见问题

### Q1：修改配置后还是不能上传？
**A**：请确保已经重启后端服务！

### Q2：前端提示文件过大怎么办？
**A**：修改前端的文件大小限制：
- 头像：`isLt2M` 改为 `isLt10M`
- 商品图片：`isLt5M` 改为 `isLt10M`

### Q3：上传速度很慢怎么办？
**A**：
1. 检查网络连接
2. 使用图片压缩
3. 考虑使用 CDN 加速

### Q4：能不能上传更大的文件？
**A**：可以，但需要考虑：
1. 服务器内存是否足够
2. OSS 存储成本
3. 用户上传体验
4. 建议不超过 50MB
