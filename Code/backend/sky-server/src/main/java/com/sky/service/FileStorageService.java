package com.sky.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务。
 */
public interface FileStorageService {

    /**
     * 上传图片并返回可访问的相对路径。
     *
     * @param file 图片文件
     * @return 图片访问路径，例如 /uploads/xxx.png
     */
    String upload(MultipartFile file);
}
