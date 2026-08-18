package com.bookcycle.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 论坛板块表。
 */
@Data
public class ForumBoard {

    private Long id;
    private String name;
    private String description;
    /** 排序值，越小越靠前 */
    private Integer sort;
    /** 板块状态：1 启用 / 0 禁用 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    /** 逻辑删除：0 正常 / 1 删除 */
    private Integer deleted;
}
