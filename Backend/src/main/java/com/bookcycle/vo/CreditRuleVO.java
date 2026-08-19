package com.bookcycle.vo;

import lombok.Data;

/**
 * 信誉规则展示 VO（管理后台系统配置）。
 */
@Data
public class CreditRuleVO {

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
}
