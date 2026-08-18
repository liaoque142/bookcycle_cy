package com.bookcycle.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友关系表：双向各一行（user_id → friend_id），status 1 正常 / 0 已删除。
 */
@Data
public class Friend {

    private Long id;
    private Long userId;
    private Long friendId;
    /** 好友状态：1 正常 / 0 已删除 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
