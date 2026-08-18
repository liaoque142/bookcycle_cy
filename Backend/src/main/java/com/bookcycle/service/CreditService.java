package com.bookcycle.service;

import com.bookcycle.common.PageResult;
import com.bookcycle.entity.CreditLog;
import com.bookcycle.entity.CreditRule;
import com.bookcycle.mapper.CreditMapper;
import com.bookcycle.vo.CreditLogVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 信誉服务：规则数据驱动分值变动（credit_rule）+ 变动日志。
 */
@Service
@RequiredArgsConstructor
public class CreditService {

    private final CreditMapper creditMapper;

    public int current(Long userId) {
        Integer credit = creditMapper.selectCredit(userId);
        return credit != null ? credit : 0;
    }

    public PageResult<CreditLogVO> logs(Long userId, int page, int size) {
        PageHelper.startPage(page, size);
        List<CreditLogVO> list = creditMapper.selectLogs(userId);
        return PageResult.of(new PageInfo<>(list));
    }

    /**
     * 按规则码触发信誉变动（规则禁用/分值为 0 时忽略）。
     * 需在调用方 @Transactional 内执行，与业务同事务。
     */
    public void apply(Long userId, String ruleCode, Long operatorId) {
        CreditRule rule = creditMapper.selectRuleByCode(ruleCode);
        if (rule == null || rule.getStatus() == null || rule.getStatus() != 1 || rule.getChangeValue() == 0) {
            return;
        }
        creditMapper.updateCredit(userId, rule.getChangeValue());
        CreditLog log = new CreditLog();
        log.setUserId(userId);
        log.setChangeValue(rule.getChangeValue());
        log.setReason(rule.getName());
        log.setOperatorId(operatorId);
        creditMapper.insertLog(log);
    }
}
