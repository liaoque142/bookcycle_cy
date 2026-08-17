package com.bookcycle.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 书籍分类（表 category）。
 */
@Data
public class Category {

    private Long id;
    /** 父分类 id，0 表示根分类 */
    private Long parentId;
    private String name;
    private Integer sort;
    /** 1 启用 / 0 禁用 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    /** 逻辑删除：0 正常 / 1 删除 */
    private Integer deleted;
}
