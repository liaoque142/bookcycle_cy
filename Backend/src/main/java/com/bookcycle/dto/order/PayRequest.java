package com.bookcycle.dto.order;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 支付入参（一期仅模拟余额，预留支付宝/微信）。
 */
@Data
public class PayRequest {

    @Min(value = 1, message = "支付方式不正确")
    @Max(value = 3, message = "支付方式不正确")
    private Integer payMethod = 1;
}
