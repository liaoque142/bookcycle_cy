package com.bookcycle.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 信誉变动日志表（可正负，管理员调整需记录操作人）。
 */
@Data
public class CreditLog {

    private Long id;
    private Long userId;
    /** 信誉变动值（正加负扣） */
    private Integer changeValue;
    private String reason;
    /** 操作人用户ID（管理员/系统触发为 NULL） */
    private Long operatorId;
    private LocalDateTime createTime;
}
