package com.bookcycle.dto.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 处理售后入参（管理员）：同意退款 / 拒绝退款。
 */
@Data
public class HandleRefundRequest {

    /** true 同意退款 / false 拒绝 */
    @NotNull(message = "处理结果不能为空")
    private Boolean agree;

    /** 处理结果说明 */
    @Size(max = 255)
    private String result;
}
