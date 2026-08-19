package com.bookcycle.dto.swap;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 同意交换入参：接收方填写收书 B 的物流方式与收货地址。
 */
@Data
public class AcceptSwapRequest {

    /** 我收书 B 的物流方式：1 邮寄 / 2 自提 */
    @NotNull(message = "物流方式不能为空")
    @Min(value = 1, message = "物流方式不正确")
    @Max(value = 2, message = "物流方式不正确")
    private Integer shipType;

    /** 邮寄时必填（自提为 null） */
    private Long addressId;
}
