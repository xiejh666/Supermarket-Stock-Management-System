# 阿里云 OSS 配置指南

## 📋 功能说明

本系统已集成阿里云对象存储 OSS，用于存储商品图片等文件，避免占用本地服务器空间。

---

## 🔧 配置步骤

### 1. 获取阿里云 OSS 信息

登录阿里云控制台，获取以下信息：

1. **Endpoint（访问域名）**
   - 进入 OSS 控制台 → 选择您的 Bucket
   - 查看 Bucket 概览页面的"访问域名"
   - 例如：`oss-cn-hangzhou.aliyuncs.com`

2. **AccessKey ID 和 AccessKey Secret**
   - 进入阿里云控制台 → 右上角头像 → AccessKey 管理
   - 创建或查看您的 AccessKey
   - ⚠️ **重要**：AccessKey Secret 只在创建时显示一次，请妥善保管

3. **Bucket 名称**
   - 您创建的 Bucket 名称
   - 例如：`supermarket-images`

4. **URL 前缀**
   - 格式：`https://您的Bucket名称.Endpoint/`
   - 例如：`https://supermarket-images.oss-cn-hangzhou.aliyuncs.com/`

### 2. 修改配置文件

打开 `application.yml`，找到 OSS 配置部分，替换为您的实际信息：

```yaml
oss:
  endpoint: oss-cn-hangzhou.aliyuncs.com  # 替换为您的 Endpoint
  access-key-id: YOUR_ACCESS_KEY_ID  # 替换为您的 AccessKey ID
  access-key-secret: YOUR_ACCESS_KEY_SECRET  # 替换为您的 AccessKey Secret
  bucket-name: YOUR_BUCKET_NAME  # 替换为您的 Bucket 名称
  url-prefix: https://YOUR_BUCKET_NAME.oss-cn-hangzhou.aliyuncs.com/  # 替换为您的 URL 前缀
```

**示例配置：**

```yaml
oss:
  endpoint: oss-cn-hangzhou.aliyuncs.com
  access-key-id: LTAI5tAbCdEfGhIjKlMnOpQr
  access-key-secret: 1234567890abcdefghijklmnopqrstuv
  bucket-name: supermarket-images
  url-prefix: https://supermarket-images.oss-cn-hangzhou.aliyuncs.com/
```

### 3. 配置 Bucket 权限

为了让上传的图片可以公开访问，需要设置 Bucket 权限：

1. 进入 OSS 控制台 → 选择您的 Bucket
2. 点击"权限管理" → "读写权限"
3. 设置为"公共读"（Public Read）

### 4. 配置跨域规则（CORS）

如果前端直接上传到 OSS，需要配置 CORS：

1. 进入 OSS 控制台 → 选择您的 Bucket
2. 点击"权限管理" → "跨域设置"
3. 添加规则：
   - 来源：`*`（或您的前端域名）
   - 允许 Methods：`GET, POST, PUT, DELETE, HEAD`
   - 允许 Headers：`*`
   - 暴露 Headers：`ETag`

---

## 🚀 使用说明

### 后端 API

**上传商品图片：**
```
POST /api/upload/product
Content-Type: multipart/form-data
Authorization: Bearer {token}

参数：
- file: 图片文件（必填）

返回：
{
  "code": 200,
  "message": "操作成功",
  "data": "https://supermarket-images.oss-cn-hangzhou.aliyuncs.com/products/uuid.jpg"
}
```

**删除文件：**
```
DELETE /api/upload?url={fileUrl}
Authorization: Bearer {token}

返回：
{
  "code": 200,
  "message": "操作成功"
}
```

### 前端使用

在商品管理页面，新增或编辑商品时：

1. 点击图片上传区域
2. 选择图片文件（支持 jpg、png 等格式）
3. 图片会自动上传到阿里云 OSS
4. 上传成功后显示图片预览
5. 保存商品时，图片 URL 会一起保存到数据库

---

## 📁 文件存储结构

上传的文件会按以下结构存储在 OSS 中：

```
Bucket根目录/
└── products/          # 商品图片目录
    ├── uuid1.jpg
    ├── uuid2.png
    └── ...
```

文件命名规则：`UUID + 原始扩展名`

---

## ⚠️ 注意事项

### 1. 安全性

- ✅ **不要**将 AccessKey 提交到 Git 仓库
- ✅ 建议使用环境变量或配置中心管理敏感信息
- ✅ 定期更换 AccessKey
- ✅ 使用 RAM 子账号，只授予必要的 OSS 权限

### 2. 费用

- 阿里云 OSS 按使用量计费（存储空间、流量、请求次数）
- 建议设置费用预警
- 可以使用生命周期规则自动删除过期文件

### 3. 文件限制

- 单个文件大小限制：5MB
- 支持的图片格式：jpg、jpeg、png、gif、bmp、webp
- 文件名会自动生成 UUID，避免重复

### 4. 性能优化

- OSS 支持 CDN 加速，可以提升图片加载速度
- 可以配置图片处理（缩略图、水印等）

---

## 🔍 常见问题

### Q1: 上传失败，提示 403 Forbidden

**原因：** AccessKey 配置错误或权限不足

**解决：**
1. 检查 AccessKey ID 和 Secret 是否正确
2. 确认 RAM 用户有 OSS 写入权限
3. 检查 Bucket 是否存在

### Q2: 图片上传成功但无法访问

**原因：** Bucket 权限设置问题

**解决：**
1. 将 Bucket 读写权限设置为"公共读"
2. 检查防盗链设置
3. 确认 URL 格式正确

### Q3: 跨域错误

**原因：** 未配置 CORS 规则

**解决：**
1. 在 OSS 控制台配置 CORS 规则
2. 允许来源设置为 `*` 或您的前端域名

### Q4: 如何删除旧图片？

**方法 1：** 在更新商品图片时，调用删除接口删除旧图片

**方法 2：** 使用 OSS 生命周期规则自动清理

---

## 📚 相关文档

- [阿里云 OSS 官方文档](https://help.aliyun.com/product/31815.html)
- [OSS Java SDK 文档](https://help.aliyun.com/document_detail/32008.html)
- [OSS 计费说明](https://help.aliyun.com/document_detail/59636.html)

---

## ✅ 配置检查清单

- [ ] 已创建阿里云 OSS Bucket
- [ ] 已获取 AccessKey ID 和 Secret
- [ ] 已在 `application.yml` 中配置 OSS 参数
- [ ] Bucket 权限已设置为"公共读"
- [ ] 已配置 CORS 规则（如需要）
- [ ] 已测试图片上传功能
- [ ] AccessKey 未提交到代码仓库

---

**配置完成后，重启后端服务即可使用！** 🎉
