package com.bookcycle.dto.social;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发表书籍讨论 / 回复：parentId 为父评论 ID（NULL 为顶级评论）。
 */
@Data
public class CreateDiscussionRequest {

    @NotBlank(message = "评论内容不能为空")
    private String content;

    /** 父评论 ID，NULL 为顶级 */
    private Long parentId;
}
