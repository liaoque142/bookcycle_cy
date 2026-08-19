package com.bookcycle.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志展示 VO（operation_log + sys_user 操作人）。
 */
@Data
public class OperationLogVO {

    private Long id;
    private String action;
    private String detail;
    /** 操作人用户ID */
    private Long operatorId;
    /** 操作人用户名（sys_user.username） */
    private String operatorName;
    private LocalDateTime createTime;
}
