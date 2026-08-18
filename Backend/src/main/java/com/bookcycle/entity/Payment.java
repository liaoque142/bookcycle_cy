package com.bookcycle.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付表（一期模拟支付，预留支付宝/微信）。
 */
@Data
public class Payment {

    private Long id;
    private Long orderId;
    private BigDecimal amount;
    /** 支付方式：1 模拟余额 / 2 支付宝 / 3 微信 */
    private Integer payMethod;
    /** 支付状态：0 未支付 / 1 已支付 / 2 已退款 */
    private Integer payStatus;
    private LocalDateTime payTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
