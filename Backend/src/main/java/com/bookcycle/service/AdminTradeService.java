package com.bookcycle.service;

import com.bookcycle.common.PageResult;
import com.bookcycle.exception.BusinessException;
import com.bookcycle.mapper.TradeMapper;
import com.bookcycle.security.SecurityUtils;
import com.bookcycle.vo.OrderVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理后台：交易订单管理（全量订单查询/详情，仅 admin 角色）。
 */
@Service
@RequiredArgsConstructor
public class AdminTradeService {

    private final TradeMapper tradeMapper;

    public PageResult<OrderVO> page(String keyword, Integer status, Integer tradeType, int page, int size) {
        PageHelper.startPage(page, size);
        List<OrderVO> list = tradeMapper.selectAdminOrderPage(keyword, status, tradeType, SecurityUtils.currentUserId());
        return PageResult.of(new PageInfo<>(list));
    }

    public OrderVO detail(String orderNo) {
        OrderVO vo = tradeMapper.selectOrderVO(orderNo, SecurityUtils.currentUserId());
        if (vo == null) {
            throw new BusinessException(404, "订单不存在");
        }
        return vo;
    }
}
