package com.bookcycle.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 信誉变动日志展示 VO。
 */
@Data
public class CreditLogVO {

    private Integer changeValue;
    private String reason;
    private LocalDateTime createTime;
}
