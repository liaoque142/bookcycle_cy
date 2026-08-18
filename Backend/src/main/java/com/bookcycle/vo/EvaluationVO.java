package com.bookcycle.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 交易评价展示 VO。
 */
@Data
public class EvaluationVO {

    private Long id;
    private Long orderId;
    private Long evaluatorId;
    private String evaluatorName;
    private Long targetId;
    private Integer score;
    private String content;
    private LocalDateTime createTime;
}
