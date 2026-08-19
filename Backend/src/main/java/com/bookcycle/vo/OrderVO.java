package com.bookcycle.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易订单展示 VO（联表：书 / 买卖双方 / 地址 / 支付，枚举码映射中文）。
 * 注意：禁止 Lombok @Builder（全参构造器会触发列顺序构造器自动映射，见 PROGRESS.md 踩坑）。
 */
@Data
public class OrderVO {

    private Long id;
    private String orderNo;
    private Long bookId;
    private String bookTitle;
    private Long buyerId;
    private String buyerName;
    private Long sellerId;
    private String sellerName;
    private BigDecimal amount;
    /** 交易方式中文：出售 / 赠送 / 交换 */
    private String tradeType;
    private Integer tradeTypeCode;
    /** 物流方式中文：邮寄 / 自提 */
    private String shipType;
    private Integer shipTypeCode;
    /** 订单状态中文：待支付 / 待发货 / 待收货 / 已完成 / 已取消 / 售后中 */
    private String status;
    private Integer statusCode;
    private String receiverName;
    private String receiverPhone;
    /** 省市区+详细地址拼接 */
    private String receiverAddress;
    private String cancelReason;
    /** 支付状态：0 未支付 / 1 已支付 / 2 已退款 */
    private Integer payStatus;
    private LocalDateTime payTime;
    /** 售后状态：0 无 / 1 申请中 / 2 处理中 / 3 已完成 */
    private Integer afterSale;
    /** 当前用户是否已评价该订单 */
    private Boolean evaluatedByMe;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
