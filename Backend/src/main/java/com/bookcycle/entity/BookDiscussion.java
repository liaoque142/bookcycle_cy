package com.bookcycle.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 书籍讨论表：书籍详情页留言 / 回复，parent_id 指向父评论（NULL 为顶级）。
 */
@Data
public class BookDiscussion {

    private Long id;
    private Long bookId;
    private Long userId;
    private String content;
    /** 图片 URL（可选，与文字可并存） */
    private String imageUrl;
    private Long parentId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    /** 逻辑删除：0 正常 / 1 删除 */
    private Integer deleted;
}
