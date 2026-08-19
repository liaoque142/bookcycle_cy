package com.bookcycle.dto.swap;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 取消交换入参。
 */
@Data
public class CancelSwapRequest {

    @Size(max = 255, message = "取消原因不能超过 255 字")
    private String reason;
}
