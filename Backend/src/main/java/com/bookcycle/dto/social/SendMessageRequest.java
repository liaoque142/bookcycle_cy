package com.bookcycle.dto.social;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发送私信：toUserId 为接收方（好友）用户 ID。
 */
@Data
public class SendMessageRequest {

    @NotNull(message = "请选择要发送的好友")
    private Long toUserId;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 500, message = "消息内容不能超过 500 字")
    private String content;
}
