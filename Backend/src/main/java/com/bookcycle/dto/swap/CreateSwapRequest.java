package com.bookcycle.dto.swap;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发起交换入参：用我的书 offerBookId 换卖家书 A；邮寄（shipType=1）必填 addressId（收书 A 的地址）。
 */
@Data
public class CreateSwapRequest {

    /** 被交换的书籍ID（卖家书 A，需 tradeType=3 交换且在售） */
    @NotNull(message = "书籍不能为空")
    private Long bookId;

    /** 我提供的书籍ID（必须是本人所有且在售） */
    @NotNull(message = "请选择用于交换的书籍")
    private Long offerBookId;

    /** 我收书 A 的物流方式：1 邮寄 / 2 自提 */
    @NotNull(message = "物流方式不能为空")
    @Min(value = 1, message = "物流方式不正确")
    @Max(value = 2, message = "物流方式不正确")
    private Integer shipType;

    /** 邮寄时必填（自提为 null） */
    private Long addressId;

    /** 附言（给对方） */
    @Size(max = 255, message = "附言不能超过 255 字")
    private String message;
}
