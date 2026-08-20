package com.bookcycle.dto.social;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发送私信：toUserId 为接收方（好友）用户 ID。
 * <p>content 与 imageUrl 至少填一项（纯文字 / 文字+图 / 纯图）。
 */
@Data
public class SendMessageRequest {

    @NotNull(message = "请选择要发送的好友")
    private Long toUserId;

    @Size(max = 500, message = "消息内容不能超过 500 字")
    private String content;

    /** 图片 URL（可选，来自 /api/v1/upload/image 上传结果） */
    @Size(max = 255, message = "图片地址过长")
    private String imageUrl;
}
