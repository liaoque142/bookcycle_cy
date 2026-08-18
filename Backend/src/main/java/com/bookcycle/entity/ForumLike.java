package com.bookcycle.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子点赞表（取消点赞即删除行，故不做逻辑删除）。
 */
@Data
public class ForumLike {

    private Long id;
    private Long postId;
    private Long userId;
    private LocalDateTime createTime;
}
