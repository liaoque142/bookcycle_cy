package com.bookcycle.dto.forum;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表评论请求。
 * <p>content 与 imageUrl 至少填一项（纯文字 / 文字+图 / 纯图）。
 */
@Data
public class CreateCommentRequest {

    @NotNull(message = "缺少帖子ID")
    private Long postId;

    @Size(max = 500, message = "评论最多 500 个字符")
    private String content;

    /** 图片 URL（可选，来自 /api/v1/upload/image 上传结果） */
    @Size(max = 255, message = "图片地址过长")
    private String imageUrl;
}
