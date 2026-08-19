package com.bookcycle.dto.order;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 申请售后入参（买家）：仅退款 / 退货退款。
 */
@Data
public class CreateRefundRequest {

    /** 售后类型：1 仅退款 / 2 退货退款 */
    @NotNull(message = "售后类型不能为空")
    @Min(value = 1, message = "售后类型不合法")
    @Max(value = 2, message = "售后类型不合法")
    private Integer type;

    @Size(max = 255)
    private String reason;
}
