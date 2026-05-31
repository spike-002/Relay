package com.spike.relay.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储抽象。v1.0 落地为本地磁盘，后续可无痛替换为 OSS/COS。
 */
public interface StorageService {

    /**
     * 存储一个文件并返回可公开访问的 URL。
     *
     * @param file      上传的文件
     * @param bizPrefix 业务前缀（如 "pickup"、"delivery"），用于文件命名归类
     * @return 可通过 HTTP 访问的完整 URL
     */
    String store(MultipartFile file, String bizPrefix);
}
