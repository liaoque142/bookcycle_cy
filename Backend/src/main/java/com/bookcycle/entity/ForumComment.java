package com.bookcycle.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子评论表。
 */
@Data
public class ForumComment {

    private Long id;
    /** 帖子ID（关联 forum_post.id） */
    private Long postId;
    /** 评论用户ID */
    private Long userId;
    private String content;
    /** 图片 URL（可选，与文字可并存） */
    private String imageUrl;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    /** 逻辑删除：0 正常 / 1 删除 */
    private Integer deleted;
}
