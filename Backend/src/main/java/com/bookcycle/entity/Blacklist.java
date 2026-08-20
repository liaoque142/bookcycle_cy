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
    /** 拉黑时是否为好友：1 是（解除拉黑时恢复好友）/ 0 否 */
    private Integer wasFriend;
    private LocalDateTime createTime;
}
