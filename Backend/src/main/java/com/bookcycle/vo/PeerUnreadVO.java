package com.bookcycle.vo;

import lombok.Data;

/**
 * 按好友未读数：好友行角标（Header 红点用总数，见 unread-count）。
 */
@Data
public class PeerUnreadVO {

    /** 发来未读消息的好友用户ID */
    private Long userId;
    /** 未读条数 */
    private Integer unreadCount;
}
