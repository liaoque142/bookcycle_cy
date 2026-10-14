package com.bookcycle.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款/售后展示 VO（联表：订单 / 书籍 / 申请人 / 处理人，枚举码映射中文）。
 * 注意：禁止 Lombok @Builder（全参构造器会触发列顺序构造器自动映射，见 DECISIONS.md「MyBatis 映射的 VO 类禁用 Lombok @Builder」）。
 */
@Data
public class RefundVO {

    private Long id;
    private Long orderId;
    private String orderNo;
    private Long bookId;
    private String bookTitle;
    private BigDecimal amount;
    /** 售后类型中文：仅退款 / 退货退款 */
    private String type;
    private Integer typeCode;
    private String reason;
    /** 处理状态中文：待处理 / 同意 / 拒绝 / 已完成 */
    private String status;
    private Integer statusCode;
    private Long applicantId;
    /** 申请人昵称（缺省回退用户名） */
    private String applicantName;
    private Long handlerId;
    /** 处理人昵称（缺省回退用户名） */
    private String handlerName;
    private String handleResult;
    private LocalDateTime createTime;
    /** 处理时间（refund.update_time） */
    private LocalDateTime handleTime;
}
