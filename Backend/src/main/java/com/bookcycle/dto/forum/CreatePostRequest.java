package com.bookcycle.dto.forum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发帖 / 编辑帖子请求（创建与编辑共用，编辑时校验所有权）。
 */
@Data
public class CreatePostRequest {

    @NotNull(message = "请选择板块")
    private Long boardId;

    @NotBlank(message = "请输入标题")
    @Size(max = 100, message = "标题最多 100 个字符")
    private String title;

    @NotBlank(message = "请输入内容")
    @Size(max = 10000, message = "内容最多 10000 个字符")
    private String content;
}
