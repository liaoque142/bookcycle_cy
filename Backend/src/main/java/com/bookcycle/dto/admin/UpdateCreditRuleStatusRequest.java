package com.bookcycle.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 信誉规则启用/禁用入参。
 */
@Data
public class UpdateCreditRuleStatusRequest {

    /** 生效状态：1 启用 / 0 禁用 */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
