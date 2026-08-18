package com.bookcycle.vo;

import lombok.Data;

/**
 * 论坛板块展示 VO。
 */
@Data
public class ForumBoardVO {

    private Long id;
    private String name;
    private String description;
    /** 排序值，越小越靠前 */
    private Integer sort;
}
