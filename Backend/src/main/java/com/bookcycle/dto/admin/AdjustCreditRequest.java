package com.bookcycle.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理后台：调整用户信誉分入参。
 */
@Data
public class AdjustCreditRequest {

    /** 信誉变动值（正加负扣），管理员手动调整 */
    @NotNull(message = "信誉变动值不能为空")
    @Min(value = -100, message = "变动值范围 -100~100")
    @Max(value = 100, message = "变动值范围 -100~100")
    private Integer changeValue;

    @NotBlank(message = "调整原因不能为空")
    @Size(max = 255, message = "原因最长 255 字符")
    private String reason;
}
