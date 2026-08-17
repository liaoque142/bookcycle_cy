package com.bookcycle.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 书籍表。
 */
@Data
public class Book {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    /** 分类ID（关联 category.id） */
    private Long categoryId;
    /** 卖家用户ID（关联 sys_user.id） */
    private Long userId;
    private BigDecimal originalPrice;
    /** 售价（交换/赠送为 NULL） */
    private BigDecimal price;
    /** 交易方式：1 出售 / 2 赠送 / 3 交换 */
    private Integer tradeType;
    /** 新旧程度：1 全新 / 2 九成新 / 3 八成新 / 4 七成新及以下 */
    private Integer condition;
    private String description;
    /** 书籍状态：1 在售 / 2 已预订 / 3 已售出 / 4 已下架 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    /** 逻辑删除：0 正常 / 1 删除 */
    private Integer deleted;
}
