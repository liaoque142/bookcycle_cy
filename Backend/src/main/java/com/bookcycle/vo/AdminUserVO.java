package com.bookcycle.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台用户展示 VO（sys_user + role + user_profile），含详情字段。
 * <p>注意：不要加 @Builder，见 BookVO 说明。
 */
@Data
public class AdminUserVO {

    private Long id;
    private String username;
    /** 昵称（缺省回退用户名） */
    private String nickname;
    private String avatar;
    private String bio;
    private String phone;
    private String email;
    /** 信誉分 */
    private Integer credit;
    /** 用户状态：1 正常 / 0 禁用 */
    private Integer status;
    private String roleCode;
    private String roleName;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
}
