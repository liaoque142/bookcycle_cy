package com.bookcycle.dto.social;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 处理好友申请：action = accept（同意）/ reject（拒绝）。
 */
@Data
public class HandleFriendRequest {

    @NotBlank(message = "请指定处理动作")
    private String action;
}
