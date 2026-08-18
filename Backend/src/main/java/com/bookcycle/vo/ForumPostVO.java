package com.bookcycle.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 论坛帖子展示 VO（forum_post + forum_board + sys_user + user_profile + 聚合回复数）。
 * 列表：excerpt 有值、content 为空；详情：content 有值。
 * <p>注意：不要加 @Builder —— 同 BookVO，Lombok 全参构造器会触发 MyBatis 列顺序构造器自动映射，
 * 由 resultMap 用 setter 填充。
 */
@Data
public class ForumPostVO {

    private Long id;
    private Long boardId;
    /** 板块名 */
    private String boardName;
    private Long authorId;
    /** 发帖人昵称（user_profile.nickname，缺省回退用户名） */
    private String author;
    private String title;
    /** 列表摘要（SQL LEFT 截取，详情为 NULL） */
    private String excerpt;
    /** 详情全文（列表为 NULL） */
    private String content;
    /** 回复数（forum_comment 聚合） */
    private Long replyCount;
    /** 点赞数 */
    private Integer likeCount;
    private Integer viewCount;
    /** 当前登录用户是否已点赞（游客恒 false） */
    private Boolean liked;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
