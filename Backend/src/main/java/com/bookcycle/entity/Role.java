package com.bookcycle.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色表（RBAC：role_code admin / user）。
 */
@Data
public class Role {

    private Long id;
    private String roleName;
    /** 角色编码：admin / user（唯一） */
    private String roleCode;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
