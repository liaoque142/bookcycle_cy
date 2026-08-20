package com.bookcycle.vo;

import lombok.Data;

/**
 * 用户摘要：搜索书友、好友/黑名单列表展示用（昵称缺省回退用户名）。
 */
@Data
public class UserBriefVO {

    private Long id;
    private String username;
    private String nickname;
    /** 头像（可空，前端回退首字） */
    private String avatar;
    private Integer credit;
}
