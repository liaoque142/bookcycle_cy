package com.bookcycle.controller;

import com.bookcycle.common.Result;
import com.bookcycle.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 图片上传接口（需登录）：评论 / 私信发图先传图拿 URL，再随内容提交。
 * <p>仅服务端业务用途，游客不可上传。
 */
@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    /** 上传图片，返回 { url: "/uploads/yyyy/MM/xxx.png" } */
    @PostMapping("/image")
    public Result<Map<String, String>> image(@RequestParam("file") MultipartFile file) {
        return Result.ok(Map.of("url", uploadService.uploadImage(file)));
    }
}
