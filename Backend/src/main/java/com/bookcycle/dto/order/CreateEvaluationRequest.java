package com.bookcycle.dto.order;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 交易评价入参（评分 1-5）。
 */
@Data
public class CreateEvaluationRequest {

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分为 1-5 分")
    @Max(value = 5, message = "评分为 1-5 分")
    private Integer score;

    @Size(max = 500)
    private String content;
}
