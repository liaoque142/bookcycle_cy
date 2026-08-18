package com.bookcycle.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子评论展示 VO（forum_comment + sys_user + user_profile）。
 */
@Data
public class ForumCommentVO {

    private Long id;
    private Long postId;
    private Long authorId;
    /** 评论人昵称（user_profile.nickname，缺省回退用户名） */
    private String author;
    private String content;
    private LocalDateTime createTime;
}
