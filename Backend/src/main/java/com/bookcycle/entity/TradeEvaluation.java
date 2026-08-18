package com.bookcycle.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 交易评价表（交易完成后互评，评分 1-5 影响信誉分）。
 */
@Data
public class TradeEvaluation {

    private Long id;
    private Long orderId;
    private Long evaluatorId;
    private Long targetId;
    private Integer score;
    private String content;
    private LocalDateTime createTime;
}
