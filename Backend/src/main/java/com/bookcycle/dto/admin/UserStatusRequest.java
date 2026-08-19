package com.bookcycle.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理后台：用户启用/禁用入参。
 */
@Data
public class UserStatusRequest {

    /** 1 正常 / 0 禁用 */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
