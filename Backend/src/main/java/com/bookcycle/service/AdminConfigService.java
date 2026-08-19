package com.bookcycle.service;

import com.bookcycle.dto.admin.UpdateCreditRuleStatusRequest;
import com.bookcycle.entity.CreditRule;
import com.bookcycle.exception.BusinessException;
import com.bookcycle.mapper.CreditMapper;
import com.bookcycle.vo.CreditRuleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理后台：系统配置（信誉规则数据驱动的启用/禁用）。
 */
@Service
@RequiredArgsConstructor
public class AdminConfigService {

    private final CreditMapper creditMapper;
    private final OperationLogService operationLogService;

    public List<CreditRuleVO> rules() {
        return creditMapper.selectAllRules().stream().map(this::toVO).toList();
    }

    /** 启用/禁用信誉规则（禁用后 CreditService.apply 自动忽略该规则） */
    @Transactional
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "状态不合法");
        }
        CreditRule rule = creditMapper.selectRuleById(id);
        if (rule == null) {
            throw new BusinessException(404, "规则不存在");
        }
        creditMapper.updateRuleStatus(id, status);
        operationLogService.record("TOGGLE_CREDIT_RULE", "信誉规则「" + rule.getName() + "」"
                + (status == 1 ? "启用" : "禁用"));
    }

    private CreditRuleVO toVO(CreditRule rule) {
        CreditRuleVO vo = new CreditRuleVO();
        vo.setId(rule.getId());
        vo.setRuleCode(rule.getRuleCode());
        vo.setName(rule.getName());
        vo.setTriggerCondition(rule.getTriggerCondition());
        vo.setChangeValue(rule.getChangeValue());
        vo.setPriority(rule.getPriority());
        vo.setStatus(rule.getStatus());
        return vo;
    }
}
