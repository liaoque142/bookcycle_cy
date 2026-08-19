package com.bookcycle.vo;

import lombok.Data;

/**
 * 按日计数（统计聚合中间结果）。
 */
@Data
public class DayCountVO {

    /** 日期 yyyy-MM-dd */
    private String date;
    /** 当日数量 */
    private Long count;
}
