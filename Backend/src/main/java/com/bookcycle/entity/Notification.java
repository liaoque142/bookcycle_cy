package com.bookcycle.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知表：用户站内信，type 1 系统 / 2 交易 / 3 好友。
 */
@Data
public class Notification {

    private Long id;
    private Long userId;
    /** 通知类型：1 系统 / 2 交易 / 3 好友 */
    private Integer type;
    private String content;
    /** 是否已读：1 已读 / 0 未读 */
    private Integer isRead;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
