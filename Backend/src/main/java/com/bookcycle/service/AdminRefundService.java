package com.bookcycle.service;

import com.bookcycle.common.PageResult;
import com.bookcycle.dto.admin.HandleRefundRequest;
import com.bookcycle.entity.Refund;
import com.bookcycle.entity.TradeOrder;
import com.bookcycle.exception.BusinessException;
import com.bookcycle.mapper.BookMapper;
import com.bookcycle.mapper.RefundMapper;
import com.bookcycle.mapper.TradeMapper;
import com.bookcycle.security.SecurityUtils;
import com.bookcycle.vo.RefundVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 管理后台：售后/退款处理（列表 + 同意/拒绝，仅 admin 角色）。
 * <p>同意：订单取消（售后完成）+ 释放书籍回在售 + 已支付自动退款 + 通知双方；
 * 拒绝：仅回退订单售后状态，买家可重新操作。
 */
@Service
@RequiredArgsConstructor
public class AdminRefundService {

    /** 书籍状态：2 已预订 / 3 已售出 */
    private static final int BOOK_RESERVED = 2;
    private static final int BOOK_SOLD = 3;

    private final RefundMapper refundMapper;
    private final TradeMapper tradeMapper;
    private final BookMapper bookMapper;
    private final NotificationService notificationService;
    private final OperationLogService operationLogService;

    public PageResult<RefundVO> page(String keyword, Integer status, Integer type, int page, int size) {
        PageHelper.startPage(page, size);
        List<RefundVO> list = refundMapper.selectPage(keyword, status, type);
        return PageResult.of(new PageInfo<>(list));
    }

    /** 处理售后：agree=true 同意退款（取消订单 + 释放书籍 + 退款），false 拒绝 */
    @Transactional
    public void handle(Long id, HandleRefundRequest req) {
        Refund refund = refundMapper.selectById(id);
        if (refund == null) {
            throw new BusinessException(404, "售后申请不存在");
        }
        if (refund.getStatus() != 1) {
            throw new BusinessException("该售后申请已处理");
        }
        TradeOrder order = tradeMapper.selectById(refund.getOrderId());
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (order.getAfterSale() == null || order.getAfterSale() != 1) {
            throw new BusinessException("订单售后状态异常");
        }
        String result = StringUtils.hasText(req.getResult()) ? req.getResult().trim()
                : (Boolean.TRUE.equals(req.getAgree()) ? "同意退款" : "拒绝退款");
        Long adminId = SecurityUtils.currentUserId();
        boolean agree = Boolean.TRUE.equals(req.getAgree());

        if (agree) {
            // 取消订单（status 5 + cancel_reason），释放书籍回在售，已支付自动退款，售后完成
            tradeMapper.cancel(order.getId(), "售后退款：" + result);
            releaseBook(order.getBookId());
            tradeMapper.refundPayment(order.getId());
            tradeMapper.updateAfterSale(order.getId(), 3);
            handleRefund(refund, 4, adminId, result);
            operationLogService.record("REFUND_HANDLE", "售后单 " + id + "（订单 " + order.getOrderNo()
                    + "）同意退款：" + result);
            notificationService.notify(order.getBuyerId(), NotificationService.TYPE_TRADE,
                    "您的售后申请已通过（订单 " + order.getOrderNo() + "），退款已原路退回");
            notificationService.notify(order.getSellerId(), NotificationService.TYPE_TRADE,
                    "售后申请已同意（订单 " + order.getOrderNo() + "），书籍已释放回在售");
        } else {
            // 拒绝：回退订单售后状态，买家可重新操作
            tradeMapper.updateAfterSale(order.getId(), 0);
            handleRefund(refund, 3, adminId, result);
            operationLogService.record("REFUND_HANDLE", "售后单 " + id + "（订单 " + order.getOrderNo()
                    + "）拒绝退款：" + result);
            notificationService.notify(order.getBuyerId(), NotificationService.TYPE_TRADE,
                    "您的售后申请未通过（订单 " + order.getOrderNo() + "）：" + result);
        }
    }

    /** refund 状态 CAS 更新（待处理 → 终态），0 行说明已被并发处理 */
    private void handleRefund(Refund refund, int status, Long handlerId, String result) {
        if (refundMapper.handle(refund.getId(), status, handlerId, result) == 0) {
            throw new BusinessException("该售后申请已处理");
        }
    }

    /** 释放书籍回在售：已预订/已售出 → 在售（退款同意后书回到卖家库存） */
    private void releaseBook(Long bookId) {
        if (bookMapper.updateStatusIfEquals(bookId, BOOK_RESERVED, 1) == 0) {
            bookMapper.updateStatusIfEquals(bookId, BOOK_SOLD, 1);
        }
    }
}
