package com.bookcycle.dto.social;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表书籍讨论 / 回复：parentId 为父评论 ID（NULL 为顶级评论）。
 * <p>content 与 imageUrl 至少填一项（纯文字 / 文字+图 / 纯图）。
 */
@Data
public class CreateDiscussionRequest {

    @Size(max = 500, message = "评论内容不能超过 500 字")
    private String content;

    /** 图片 URL（可选，来自 /api/v1/upload/image 上传结果） */
    @Size(max = 255, message = "图片地址过长")
    private String imageUrl;

    /** 父评论 ID，NULL 为顶级 */
    private Long parentId;
}
