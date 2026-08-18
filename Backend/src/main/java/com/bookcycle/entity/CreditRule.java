package com.bookcycle.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 信誉规则表（数据驱动，可后台配置）。
 */
@Data
public class CreditRule {

    private Long id;
    /** 规则标识（唯一），如 TRADE_SUCCESS */
    private String ruleCode;
    private String name;
    private String triggerCondition;
    /** 分值（正加负扣），如 +2 / -5 */
    private Integer changeValue;
    /** 优先级，越小越先匹配 */
    private Integer priority;
    /** 生效状态：1 启用 / 0 禁用 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
