package com.spike.relay.service.impl;

import com.spike.relay.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class LocalStorageServiceImpl implements StorageService {

    /** 文件落盘的根目录，绝对路径或相对运行目录 */
    @Value("${storage.local.dir}")
    private String storageDir;

    /** 对外访问前缀，形如 http://localhost:8080/api/uploads */
    @Value("${storage.local.url-prefix}")
    private String urlPrefix;

    @Override
    public String store(MultipartFile file, String bizPrefix) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("只允许上传图片文件");
        }

        // 按日期分目录，避免单目录文件过多
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String filename = buildFilename(bizPrefix, file.getOriginalFilename());

        try {
            Path dir = Paths.get(storageDir, datePath);
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            file.transferTo(target.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("文件存储失败：" + e.getMessage(), e);
        }

        // URL 与磁盘 datePath 子目录保持一致
        return urlPrefix + "/" + datePath + "/" + filename;
    }

    private String buildFilename(String bizPrefix, String originalFilename) {
        String ext = StringUtils.getFilenameExtension(originalFilename);
        String safeExt = (ext != null && !ext.isBlank()) ? "." + ext.toLowerCase() : ".jpg";
        String prefix = (bizPrefix != null && !bizPrefix.isBlank()) ? bizPrefix : "file";
        long ts = System.currentTimeMillis();
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return prefix + "_" + ts + "_" + random + safeExt;
    }
}
