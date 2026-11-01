package com.sky.service.impl;

import com.sky.exception.BusinessException;
import com.sky.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 本地图片存储实现。
 *
 * <p>当前把图片存到 sky.upload.dir（默认 ./uploads），返回 /uploads/xxx 路径；
 * 后续接入阿里云 OSS 时只需要替换这个实现。</p>
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    /** 允许上传的图片扩展名 */
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "gif", "webp");

    @Value("${sky.upload.dir:./uploads}")
    private String uploadDir;

    /**
     * 保存图片到本地目录。
     *
     * @param file 图片文件
     * @return 图片访问路径
     */
    @Override
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new BusinessException("只支持 jpg、jpeg、png、gif、webp 图片");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("上传文件必须是图片");
        }

        Path directory = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(directory);
            String filename = UUID.randomUUID().toString().replace("-", "")
                    + "." + extension.toLowerCase(Locale.ROOT);
            Path target = directory.resolve(filename).normalize();
            // 防止文件名穿越目录
            if (!target.startsWith(directory)) {
                throw new BusinessException("文件名不合法");
            }
            file.transferTo(target.toFile());
            return "/uploads/" + filename;
        } catch (IOException | IllegalStateException ex) {
            throw new BusinessException("图片上传失败");
        }
    }
}
