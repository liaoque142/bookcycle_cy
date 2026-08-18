package com.bookcycle.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友申请（接收方视角）：from 字段为申请人信息。
 */
@Data
public class FriendRequestVO {

    private Long id;
    private Long fromUserId;
    private String fromUsername;
    private String fromNickname;
    private Integer fromCredit;
    private Long toUserId;
    /** 0 待处理 / 1 同意 / 2 拒绝 */
    private Integer status;
    private LocalDateTime createTime;
}
