package com.bookcycle.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 私信消息：双方（sender/receiver）之一视角均可读，第三者不可见。
 */
@Data
public class MessageVO {

    private Long id;
    /** 发送人用户ID */
    private Long senderId;
    /** 接收人用户ID */
    private Long receiverId;
    private String content;
    /** 图片 URL（可选，与文字可并存） */
    private String imageUrl;
    /** 是否已读：1 已读 / 0 未读 */
    private Integer isRead;
    private LocalDateTime createTime;
}
