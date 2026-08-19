package com.bookcycle.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理后台统计概览（概览卡片数据）。
 */
@Data
public class StatsOverviewVO {

    /** 用户总数（含 admin） */
    private Long userCount;
    /** 书籍总数 */
    private Long bookCount;
    /** 订单总数 */
    private Long orderCount;
    /** 今日订单数 */
    private Long todayOrders;
    /** 今日新增书友 */
    private Long todayNewUsers;
    /** 累计交易额（待收货 + 已完成） */
    private BigDecimal totalAmount;
    /** 待处理售后 */
    private Long pendingRefund;
}
