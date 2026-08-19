package com.bookcycle.service;

import com.bookcycle.common.PageResult;
import com.bookcycle.entity.OperationLog;
import com.bookcycle.mapper.OperationLogMapper;
import com.bookcycle.security.SecurityUtils;
import com.bookcycle.vo.OperationLogVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作日志服务：管理员关键操作记录 + 查询。
 * <p>记录由各管理 Service 在 @Transactional 内调用，与业务同事务，保证一致性。
 */
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;

    /** 记录一条操作日志（操作人 = 当前登录管理员） */
    public void record(String action, String detail) {
        OperationLog log = new OperationLog();
        log.setOperatorId(SecurityUtils.currentUserId());
        log.setAction(action);
        log.setDetail(detail);
        operationLogMapper.insert(log);
    }

    public PageResult<OperationLogVO> page(String action, Long operatorId, int page, int size) {
        PageHelper.startPage(page, size);
        List<OperationLogVO> list = operationLogMapper.selectPage(action, operatorId);
        return PageResult.of(new PageInfo<>(list));
    }
}
