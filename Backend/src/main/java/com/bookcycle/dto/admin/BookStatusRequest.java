package com.bookcycle.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理后台：书籍下架/上架入参。
 */
@Data
public class BookStatusRequest {

    /** 1 上架（重新在售）/ 4 下架 */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
