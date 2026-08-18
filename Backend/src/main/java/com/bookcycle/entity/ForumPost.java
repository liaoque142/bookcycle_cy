package com.bookcycle.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 论坛帖子表。
 */
@Data
public class ForumPost {

    private Long id;
    /** 板块ID（关联 forum_board.id） */
    private Long boardId;
    /** 发帖用户ID */
    private Long userId;
    private String title;
    private String content;
    /** 点赞数（冗余，以 forum_like 为准，点赞/取消时事务同步） */
    private Integer likeCount;
    private Integer viewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    /** 逻辑删除：0 正常 / 1 删除 */
    private Integer deleted;
}
