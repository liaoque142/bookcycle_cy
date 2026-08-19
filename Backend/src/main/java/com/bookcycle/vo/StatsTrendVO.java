package com.bookcycle.vo;

import lombok.Data;

/**
 * 每日统计趋势（近 N 天订单数 / 新增用户数）。
 */
@Data
public class StatsTrendVO {

    /** 日期 yyyy-MM-dd */
    private String date;
    /** 当日订单数 */
    private Long orderCount;
    /** 当日新增用户数 */
    private Long userCount;
}
