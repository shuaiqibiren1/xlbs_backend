package com.itheima.controller;

import com.itheima.pojo.Result;
import com.itheima.utils.AliOssUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@RestController
public class NiiFileController {

    @PostMapping("/niiupload")
    public Result<String> uploadtest(@RequestParam("file") MultipartFile file) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            return Result.error("File is empty. Please upload a valid file.");
        }

        // 获取并检查文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null ||
                !(originalFilename.toLowerCase().endsWith(".nii") || originalFilename.toLowerCase().endsWith(".nii.gz"))) {
            return Result.error("Invalid file type. Please upload a file with .nii or .nii.gz extension.");
        }

        // 生成唯一文件名
        String objectName = UUID.randomUUID().toString() + "_" + originalFilename;

        try (InputStream inputStream = file.getInputStream()) {
            // 直接上传文件流到云端
            String uploadedUrl = AliOssUtil.uploadFile(objectName, inputStream);
            return Result.success(uploadedUrl);
        } catch (Exception e) {
            return Result.error("Failed to upload file: " + e.getMessage());
        }
    }
}
