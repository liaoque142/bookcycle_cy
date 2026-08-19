package com.bookcycle.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 交换单展示 VO（swap + 书 A + 书 B + 双方昵称 + 双方收货地址）。
 * 注意：不要加 @Builder —— Lombok 全参构造器会触发 MyBatis 列顺序构造器自动映射（见 BookVO 注释）。
 */
@Data
public class SwapVO {

    private Long id;
    private String swapNo;
    /** 状态：待对方同意 / 已同意待发货 / 交换中 / 已完成 / 已取消 / 已拒绝 */
    private String status;
    /** 状态码：1-6 */
    private Integer statusCode;
    private Long initiatorId;
    /** 发起人昵称（缺省回退用户名） */
    private String initiatorName;
    private Long receiverId;
    /** 接收方昵称（缺省回退用户名） */
    private String receiverName;
    /** 被交换书籍（卖家书 A） */
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    /** 发起人提供的书籍（买家书 B） */
    private Long offerBookId;
    private String offerBookTitle;
    private String offerBookAuthor;
    /** 发起人附言 */
    private String message;
    /** 物流方式：邮寄 / 自提 */
    private String initiatorShipType;
    private Integer initiatorShipTypeCode;
    private String receiverShipType;
    private Integer receiverShipTypeCode;
    /** 发起人收货地址（收书 A） */
    private String initiatorReceiverName;
    private String initiatorReceiverPhone;
    private String initiatorReceiverAddress;
    /** 接收方收货地址（收书 B） */
    private String receiverReceiverName;
    private String receiverReceiverPhone;
    private String receiverReceiverAddress;
    private LocalDateTime initiatorShippedAt;
    private LocalDateTime receiverShippedAt;
    private LocalDateTime initiatorConfirmedAt;
    private LocalDateTime receiverConfirmedAt;
    private String cancelReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
