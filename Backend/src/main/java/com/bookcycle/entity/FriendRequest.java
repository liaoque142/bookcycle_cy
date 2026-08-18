package com.bookcycle.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友申请表：from_user_id 发起 → to_user_id 接收，status 0 待处理 / 1 同意 / 2 拒绝。
 */
@Data
public class FriendRequest {

    private Long id;
    private Long fromUserId;
    private Long toUserId;
    /** 申请状态：0 待处理 / 1 同意 / 2 拒绝 */
    private Integer status;
    private LocalDateTime createTime;
}
