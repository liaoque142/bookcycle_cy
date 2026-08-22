package com.bookcycle.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏表：用户收藏书籍，取消收藏即删除行（不做逻辑删除）。
 */
@Data
public class Favorite {

    private Long id;
    private Long userId;
    private Long bookId;
    private LocalDateTime createTime;
}
