package com.bookcycle.dto.forum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表评论请求。
 */
@Data
public class CreateCommentRequest {

    @NotNull(message = "缺少帖子ID")
    private Long postId;

    @NotBlank(message = "请输入评论内容")
    @Size(max = 500, message = "评论最多 500 个字符")
    private String content;
}
