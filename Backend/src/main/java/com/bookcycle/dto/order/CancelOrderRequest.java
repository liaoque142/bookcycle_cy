package com.bookcycle.dto.order;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 取消订单入参。
 */
@Data
public class CancelOrderRequest {

    @Size(max = 255)
    private String reason;
}
