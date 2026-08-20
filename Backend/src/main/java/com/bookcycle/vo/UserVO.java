package com.bookcycle.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 用户展示 VO（sys_user + user_profile + role），对齐前端 User 契约。
 */
@Data
@Builder
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    /** 个人简介 */
    private String bio;
    /** 兴趣标签，逗号分隔 */
    private String interestTags;
    /** 书籍喜好分类ID（由 user_profile.genre_ids 逗号串解析） */
    private List<Long> genreIds;
    /** 信誉分 0-100 */
    private Integer credit;
    /** 角色编码：admin / user */
    private String role;
}
