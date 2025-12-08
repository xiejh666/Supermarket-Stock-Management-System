package com.supermarket.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.supermarket.config.OssConfig;
import com.supermarket.exception.BusinessException;
import com.supermarket.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * OSS文件上传服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssServiceImpl implements OssService {
    
    private final OSS ossClient;
    private final OssConfig ossConfig;
    
    @Override
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new BusinessException("文件名不能为空");
            }
            
            // 获取文件扩展名
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf(".");
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex);
            }
            
            // 生成唯一文件名：文件夹/UUID.扩展名
            String fileName = folder + UUID.randomUUID().toString() + extension;
            
            // 获取文件输入流
            InputStream inputStream = file.getInputStream();
            
            // 创建PutObjectRequest对象
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                ossConfig.getBucketName(),
                fileName,
                inputStream
            );
            
            // 上传文件到OSS
            ossClient.putObject(putObjectRequest);
            
            // 关闭输入流
            inputStream.close();
            
            // 返回文件访问URL
            String fileUrl = ossConfig.getUrlPrefix() + fileName;
            log.info("文件上传成功：{}", fileUrl);
            
            return fileUrl;
            
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败：" + e.getMessage());
        }
    }
    
    @Override
    public void deleteFile(String fileUrl) {
        try {
            // 从URL中提取文件名
            String fileName = fileUrl.replace(ossConfig.getUrlPrefix(), "");
            
            // 删除文件
            ossClient.deleteObject(ossConfig.getBucketName(), fileName);
            
            log.info("文件删除成功：{}", fileUrl);
            
        } catch (Exception e) {
            log.error("文件删除失败", e);
            throw new BusinessException("文件删除失败：" + e.getMessage());
        }
    }
}
