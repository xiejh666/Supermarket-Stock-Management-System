package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.service.OssService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 */
@Api(tags = "文件上传管理")
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {
    
    private final OssService ossService;
    
    /**
     * 上传商品图片
     */
    @ApiOperation("上传商品图片")
    @PostMapping("/product")
    public Result<String> uploadProductImage(@RequestParam("file") MultipartFile file) {
        // 验证文件
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只能上传图片文件");
        }
        
        // 验证文件大小（限制5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error("图片大小不能超过5MB");
        }
        
        // 上传到OSS
        String fileUrl = ossService.uploadFile(file, "products/");
        
        return Result.success(fileUrl);
    }
    
    /**
     * 上传用户头像
     */
    @ApiOperation("上传用户头像")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        // 验证文件
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只能上传图片文件");
        }
        
        // 验证文件大小（限制2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            return Result.error("图片大小不能超过2MB");
        }
        
        // 上传到OSS
        String fileUrl = ossService.uploadFile(file, "avatars/");
        
        return Result.success(fileUrl);
    }
    
    /**
     * 删除文件
     */
    @ApiOperation("删除文件")
    @DeleteMapping
    public Result<Void> deleteFile(@RequestParam("url") String fileUrl) {
        ossService.deleteFile(fileUrl);
        return Result.success();
    }
}
