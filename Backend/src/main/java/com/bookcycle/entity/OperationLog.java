package com.bookcycle.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志表：记录管理员关键操作（plan.md §3.6 操作日志）。
 */
@Data
public class OperationLog {

    private Long id;
    /** 操作人用户ID（管理员） */
    private Long operatorId;
    /** 操作类型，如 UPDATE_USER / DOWN_BOOK */
    private String action;
    /** 操作详情 */
    private String detail;
    private LocalDateTime createTime;
}
