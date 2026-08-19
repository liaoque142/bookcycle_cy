package com.bookcycle.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理后台：分类新增/编辑入参。
 */
@Data
public class CategoryRequest {

    /** 父分类 id，0 或空表示根分类 */
    private Long parentId;

    @NotBlank(message = "分类名不能为空")
    @Size(max = 50, message = "分类名最长 50 字符")
    private String name;

    /** 排序值，越小越靠前（可空，默认 0） */
    private Integer sort;

    /** 1 启用 / 0 禁用（可空，默认 1） */
    private Integer status;
}
