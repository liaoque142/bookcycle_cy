package com.bookcycle.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易订单表（出售/赠送 一期；交换二期）。
 */
@Data
public class TradeOrder {

    private Long id;
    private String orderNo;
    private Long buyerId;
    private Long sellerId;
    private Long bookId;
    /** 订单金额（赠送为 0） */
    private BigDecimal amount;
    /** 交易方式：1 出售 / 2 赠送 / 3 交换 */
    private Integer tradeType;
    /** 物流方式：1 邮寄 / 2 自提 */
    private Integer shipType;
    /** 订单状态：1 待支付 / 2 待发货 / 3 待收货 / 4 已完成 / 5 已取消 / 6 售后中 */
    private Integer status;
    private Long addressId;
    private String cancelReason;
    /** 售后状态：0 无 / 1 申请中 / 2 处理中 / 3 已完成 */
    private Integer afterSale;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    /** 逻辑删除：0 正常 / 1 删除 */
    private Integer deleted;
}
