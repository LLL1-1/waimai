package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 管理端通用接口。
 *
 * <p>目前提供图片上传；接口在 /admin/** 下，需要 JWT token。</p>
 */
@RestController
@RequestMapping("/admin/common")
@RequiredArgsConstructor
@Tag(name = "通用接口")
public class CommonController {

    private final FileStorageService fileStorageService;

    /**
     * 上传图片。
     *
     * @param file 图片文件
     * @return 图片访问路径
     */
    @PostMapping("/upload")
    @Operation(summary = "上传图片")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(fileStorageService.upload(file));
    }
}
