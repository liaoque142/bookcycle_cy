package com.bookcycle.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款/售后表：买家申请，管理后台处理。
 */
@Data
public class Refund {

    private Long id;
    private Long orderId;
    /** 申请方用户ID（买家） */
    private Long applicantId;
    /** 售后类型：1 仅退款 / 2 退货退款 */
    private Integer type;
    private String reason;
    /** 退款金额 */
    private BigDecimal amount;
    /** 处理状态：1 待处理 / 2 同意 / 3 拒绝 / 4 已完成 */
    private Integer status;
    /** 处理人（管理员）用户ID */
    private Long handlerId;
    private String handleResult;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
