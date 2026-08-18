package com.bookcycle.dto.order;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 下单入参：bookId + 物流方式；邮寄（shipType=1）必填 addressId，自提可不填。
 */
@Data
public class CreateOrderRequest {

    @NotNull(message = "书籍不能为空")
    private Long bookId;

    @NotNull(message = "物流方式不能为空")
    @Min(value = 1, message = "物流方式不正确")
    @Max(value = 2, message = "物流方式不正确")
    private Integer shipType;

    /** 邮寄时必填（自提为 null） */
    private Long addressId;
}
