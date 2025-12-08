package com.supermarket.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * OSS文件上传服务接口
 */
public interface OssService {
    
    /**
     * 上传文件到OSS
     * @param file 文件
     * @param folder 文件夹路径（如：products/）
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String folder);
    
    /**
     * 删除OSS文件
     * @param fileUrl 文件URL
     */
    void deleteFile(String fileUrl);
}
