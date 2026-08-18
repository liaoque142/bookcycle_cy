package com.bookcycle.dto.social;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发送好友申请：toUserId 为接收方用户 ID。
 */
@Data
public class SendFriendRequest {

    @NotNull(message = "请选择要添加的用户")
    private Long toUserId;
}
