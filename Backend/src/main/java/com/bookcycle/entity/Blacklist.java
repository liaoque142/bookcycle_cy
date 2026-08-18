package com.bookcycle.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户拉黑表：user_id 拉黑 black_user_id，被拉黑后双方无法互动。
 */
@Data
public class Blacklist {

    private Long id;
    private Long userId;
    private Long blackUserId;
    private LocalDateTime createTime;
}
