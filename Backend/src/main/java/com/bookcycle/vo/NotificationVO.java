package com.bookcycle.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知：type 1 系统 / 2 交易 / 3 好友，typeName 为中文展示。
 */
@Data
public class NotificationVO {

    private Long id;
    private Integer type;
    private String typeName;
    private String content;
    /** 是否已读：1 已读 / 0 未读 */
    private Integer isRead;
    private LocalDateTime createTime;
}
