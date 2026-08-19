package com.bookcycle.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 交换单表（以书换书：买家以书 B 换卖家书 A）。
 * 状态机：1 待对方同意 / 2 已同意待发货 / 3 交换中 / 4 已完成 / 5 已取消 / 6 已拒绝；
 * 双方各自发货/确认收货，用 shipped_at / confirmed_at 记录。
 */
@Data
public class Swap {

    private Long id;
    private String swapNo;
    /** 发起人（买家）用户ID */
    private Long initiatorId;
    /** 接收方（卖家）用户ID */
    private Long receiverId;
    /** 被交换书籍ID（卖家书 A） */
    private Long bookId;
    /** 发起人提供的书籍ID（买家书 B） */
    private Long offerBookId;
    /** 发起人附言 */
    private String message;
    /** 状态：1 待对方同意 / 2 已同意待发货 / 3 交换中 / 4 已完成 / 5 已取消 / 6 已拒绝 */
    private Integer status;
    /** 发起人物流方式：1 邮寄 / 2 自提 */
    private Integer initiatorShipType;
    /** 发起人收货地址ID（收书 A） */
    private Long initiatorAddressId;
    /** 接收方物流方式：1 邮寄 / 2 自提 */
    private Integer receiverShipType;
    /** 接收方收货地址ID（收书 B） */
    private Long receiverAddressId;
    private LocalDateTime initiatorShippedAt;
    private LocalDateTime receiverShippedAt;
    private LocalDateTime initiatorConfirmedAt;
    private LocalDateTime receiverConfirmedAt;
    private String cancelReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    /** 逻辑删除：0 正常 / 1 删除 */
    private Integer deleted;
}
