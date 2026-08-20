package com.bookcycle.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友私信表：好友间一对一私信，仅双方可见（纯追加，无逻辑删除）。
 */
@Data
public class Message {

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
