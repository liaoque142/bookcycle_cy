package com.bookcycle.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表（RBAC 统一账号：用户与管理员同表，无独立 admin 表）。
 */
@Data
public class SysUser {

    private Long id;
    /** 用户名（唯一），注册时复用账号（手机号/邮箱） */
    private String username;
    /** 密码（BCrypt 哈希） */
    private String password;
    /** 手机号（唯一，登录凭证） */
    private String phone;
    /** 邮箱（唯一，登录凭证） */
    private String email;
    /** 信誉分：初始 100，0-100 */
    private Integer credit;
    /** 用户状态：1 正常 / 0 禁用 */
    private Integer status;
    /** 角色ID（关联 role.id） */
    private Long roleId;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    /** 逻辑删除：0 正常 / 1 删除 */
    private Integer deleted;
}
