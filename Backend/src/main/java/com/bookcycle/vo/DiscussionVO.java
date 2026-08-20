package com.bookcycle.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 书籍讨论：顶级评论含 replies 子回复列表。
 */
@Data
public class DiscussionVO {

    private Long id;
    private Long bookId;
    private Long userId;
    /** 评论人昵称（缺省回退用户名） */
    private String author;
    private String content;
    /** 图片 URL（可选，与文字可并存） */
    private String imageUrl;
    /** 父评论 ID，NULL 为顶级 */
    private Long parentId;
    private LocalDateTime createTime;
    /** 子回复列表（仅顶级评论有值） */
    private List<DiscussionVO> replies;
}
