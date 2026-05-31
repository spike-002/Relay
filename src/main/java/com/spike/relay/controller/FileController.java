package com.spike.relay.controller;

import com.spike.relay.common.Result;
import com.spike.relay.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private StorageService storageService;

    /**
     * 通用图片上传。bizPrefix 用于区分留痕类型（pickup/delivery），默认 file。
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "bizPrefix", defaultValue = "file") String bizPrefix) {
        try {
            String url = storageService.store(file, bizPrefix);
            return Result.success("上传成功", url);
        } catch (IllegalArgumentException e) {
            return Result.fail(400, e.getMessage());
        }
    }
}
