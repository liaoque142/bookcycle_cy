package com.bookcycle.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 书籍展示 VO（book + category + sys_user + user_profile），对齐前端 Book 契约。
 * tradeType / condition 为中文展示（枚举码由 SQL 映射）。
 * <p>注意：不要加 @Builder —— Lombok 的全参构造器会触发 MyBatis 列顺序构造器自动映射，
 * 导致 create_time 被按位置塞给 Long 参数报错；本类由 MyBatis resultMap 用 setter 填充。
 */
@Data
public class BookVO {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    /** 分类ID */
    private Long categoryId;
    /** 分类名 */
    private String category;
    private BigDecimal originalPrice;
    /** 售价（交换/赠送为 NULL） */
    private BigDecimal price;
    /** 交易方式：出售 / 赠送 / 交换 */
    private String tradeType;
    /** 新旧程度：全新 / 九成新 / 八成新 / 七成新及以下 */
    private String condition;
    private String description;
    /** 书籍状态：1 在售 / 2 已预订 / 3 已售出 / 4 已下架 */
    private Integer status;
    /** 书籍状态中文：在售 / 已预订 / 已售出 / 已下架（SQL CASE 映射） */
    private String statusName;
    /** 卖家ID */
    private Long sellerId;
    /** 卖家昵称（user_profile.nickname） */
    private String seller;
    /** 卖家头像（可空，前端回退首字） */
    private String sellerAvatar;
    /** 卖家信誉分 */
    private Integer credit;
    /** 当前用户是否已收藏（仅详情查询填充，列表为 null） */
    private Boolean favorited;
    private LocalDateTime createdAt;
}
