package com.bookcycle.service;

import com.bookcycle.common.PageResult;
import com.bookcycle.dto.order.CancelOrderRequest;
import com.bookcycle.dto.order.CreateEvaluationRequest;
import com.bookcycle.dto.order.CreateOrderRequest;
import com.bookcycle.dto.order.PayRequest;
import com.bookcycle.entity.Book;
import com.bookcycle.entity.Payment;
import com.bookcycle.entity.SysUser;
import com.bookcycle.entity.TradeEvaluation;
import com.bookcycle.entity.TradeOrder;
import com.bookcycle.exception.BusinessException;
import com.bookcycle.mapper.AddressMapper;
import com.bookcycle.mapper.BookMapper;
import com.bookcycle.mapper.EvaluationMapper;
import com.bookcycle.mapper.TradeMapper;
import com.bookcycle.mapper.UserMapper;
import com.bookcycle.vo.EvaluationVO;
import com.bookcycle.vo.OrderVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 交易服务：下单（防超卖 CAS）/ 模拟支付 / 发货 / 确认收货 / 取消 / 互评 + 信誉联动。
 */
@Service
@RequiredArgsConstructor
public class TradeService {

    private static final int BOOK_ON_SALE = 1;
    private static final int BOOK_RESERVED = 2;
    private static final int BOOK_SOLD = 3;

    /** 订单状态：1 待支付 / 2 待发货 / 3 待收货 / 4 已完成 / 5 已取消 / 6 售后中 */
    private static final int ORDER_PENDING_PAY = 1;
    private static final int ORDER_PENDING_SHIP = 2;
    private static final int ORDER_SHIPPED = 3;
    private static final int ORDER_COMPLETED = 4;
    private static final int ORDER_CANCELLED = 5;

    private final TradeMapper tradeMapper;
    private final BookMapper bookMapper;
    private final UserMapper userMapper;
    private final AddressMapper addressMapper;
    private final EvaluationMapper evaluationMapper;
    private final CreditService creditService;

    /** 下单：防超卖 CAS（在售→已预订），出售待支付 / 赠送直接待发货 */
    @Transactional
    public OrderVO createOrder(CreateOrderRequest req, Long buyerId) {
        SysUser buyer = userMapper.selectById(buyerId);
        if (buyer == null) {
            throw new BusinessException("用户不存在");
        }
        int credit = buyer.getCredit() != null ? buyer.getCredit() : 0;
        if (credit < 40) {
            throw new BusinessException("信誉分低于 40，交易功能已冻结");
        }
        Book book = bookMapper.selectEntityById(req.getBookId());
        if (book == null || book.getStatus() != BOOK_ON_SALE) {
            throw new BusinessException("书籍不存在或已被预订");
        }
        if (book.getUserId().equals(buyerId)) {
            throw new BusinessException("不能购买自己发布的书籍");
        }
        if (book.getTradeType() == 1 && credit < 60) {
            throw new BusinessException("信誉分低于 60，暂无法下单购买");
        }
        int tradeType = book.getTradeType();
        Long addressId = null;
        if (req.getShipType() == 1) { // 邮寄
            if (req.getAddressId() == null) {
                throw new BusinessException("邮寄订单请选择收货地址");
            }
            var address = addressMapper.selectById(req.getAddressId());
            if (address == null || !address.getUserId().equals(buyerId)) {
                throw new BusinessException("收货地址不存在或无权使用");
            }
            addressId = req.getAddressId();
        }
        // 防超卖：CAS 在售→已预订，0 行说明已被他人预订
        if (bookMapper.updateStatusIfEquals(book.getId(), BOOK_ON_SALE, BOOK_RESERVED) == 0) {
            throw new BusinessException("手慢了，该书已被预订");
        }
        BigDecimal amount = (tradeType == 1 && book.getPrice() != null) ? book.getPrice() : BigDecimal.ZERO;
        TradeOrder order = new TradeOrder();
        order.setOrderNo(generateOrderNo());
        order.setBuyerId(buyerId);
        order.setSellerId(book.getUserId());
        order.setBookId(book.getId());
        order.setAmount(amount);
        order.setTradeType(tradeType);
        order.setShipType(req.getShipType());
        order.setStatus(tradeType == 1 ? ORDER_PENDING_PAY : ORDER_PENDING_SHIP);
        order.setAddressId(addressId);
        order.setAfterSale(0);
        tradeMapper.insertOrder(order);
        return detail(order.getOrderNo(), buyerId);
    }

    /** 模拟支付：待支付 → 待发货，写 payment 记录 */
    @Transactional
    public OrderVO pay(String orderNo, PayRequest req, Long userId) {
        TradeOrder order = requireOrder(orderNo);
        requireBuyer(order, userId);
        if (tradeMapper.updateStatus(order.getId(), ORDER_PENDING_PAY, ORDER_PENDING_SHIP) == 0) {
            throw new BusinessException("当前订单状态不可支付");
        }
        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setAmount(order.getAmount());
        payment.setPayMethod(req.getPayMethod());
        payment.setPayStatus(1);
        payment.setPayTime(LocalDateTime.now());
        tradeMapper.insertPayment(payment);
        return detail(orderNo, userId);
    }

    /** 取消：待支付/待发货可取消；已支付自动模拟退款；买家待发货取消扣信誉（恶意取消） */
    @Transactional
    public OrderVO cancel(String orderNo, CancelOrderRequest req, Long userId) {
        TradeOrder order = requireOrder(orderNo);
        boolean isBuyer = order.getBuyerId().equals(userId);
        boolean isSeller = order.getSellerId().equals(userId);
        if (!isBuyer && !isSeller) {
            throw new BusinessException(403, "无权操作该订单");
        }
        int status = order.getStatus();
        if (status == ORDER_CANCELLED) {
            throw new BusinessException("订单已取消");
        }
        if (status == ORDER_COMPLETED) {
            throw new BusinessException("订单已完成，无法取消");
        }
        if (status == ORDER_SHIPPED) {
            throw new BusinessException("订单已发货，请走售后流程");
        }
        if (status == 6) {
            throw new BusinessException("订单售后中，无法取消");
        }
        tradeMapper.cancel(order.getId(), req.getReason());
        // 释放书籍预订，回到在售
        bookMapper.updateStatusIfEquals(order.getBookId(), BOOK_RESERVED, BOOK_ON_SALE);
        // 已支付（待发货）取消 → 模拟退款；买家恶意取消扣信誉
        if (status == ORDER_PENDING_SHIP) {
            tradeMapper.refundPayment(order.getId());
            if (isBuyer) {
                creditService.apply(userId, "MALICIOUS_CANCEL", null);
            }
        }
        return detail(orderNo, userId);
    }

    /** 卖家发货：待发货 → 待收货 */
    @Transactional
    public OrderVO ship(String orderNo, Long userId) {
        TradeOrder order = requireOrder(orderNo);
        if (!order.getSellerId().equals(userId)) {
            throw new BusinessException(403, "只有卖家可以发货");
        }
        if (tradeMapper.updateStatus(order.getId(), ORDER_PENDING_SHIP, ORDER_SHIPPED) == 0) {
            throw new BusinessException("当前订单状态不可发货");
        }
        return detail(orderNo, userId);
    }

    /** 买家确认收货：待收货 → 已完成，书籍转已售出，双方信誉 +2（TRADE_SUCCESS） */
    @Transactional
    public OrderVO confirm(String orderNo, Long userId) {
        TradeOrder order = requireOrder(orderNo);
        requireBuyer(order, userId);
        if (tradeMapper.updateStatus(order.getId(), ORDER_SHIPPED, ORDER_COMPLETED) == 0) {
            throw new BusinessException("当前订单状态不可确认收货");
        }
        bookMapper.updateStatusIfEquals(order.getBookId(), BOOK_RESERVED, BOOK_SOLD);
        creditService.apply(order.getBuyerId(), "TRADE_SUCCESS", null);
        creditService.apply(order.getSellerId(), "TRADE_SUCCESS", null);
        return detail(orderNo, userId);
    }

    /** 交易互评：完成后各评一次（uk 幂等），好评（≥4 分）给被评方信誉 +1 */
    @Transactional
    public void evaluate(String orderNo, CreateEvaluationRequest req, Long userId) {
        TradeOrder order = requireOrder(orderNo);
        if (order.getStatus() != ORDER_COMPLETED) {
            throw new BusinessException("订单完成后才能评价");
        }
        boolean isBuyer = order.getBuyerId().equals(userId);
        boolean isSeller = order.getSellerId().equals(userId);
        if (!isBuyer && !isSeller) {
            throw new BusinessException(403, "无权评价该订单");
        }
        if (evaluationMapper.selectByOrderEvaluator(order.getId(), userId) != null) {
            throw new BusinessException("您已评价过该订单");
        }
        Long targetId = isBuyer ? order.getSellerId() : order.getBuyerId();
        TradeEvaluation evaluation = new TradeEvaluation();
        evaluation.setOrderId(order.getId());
        evaluation.setEvaluatorId(userId);
        evaluation.setTargetId(targetId);
        evaluation.setScore(req.getScore());
        evaluation.setContent(StringUtils.hasText(req.getContent()) ? req.getContent().trim() : null);
        evaluationMapper.insert(evaluation);
        if (req.getScore() >= 4) {
            creditService.apply(targetId, "GOOD_REVIEW", null);
        }
    }

    public PageResult<OrderVO> list(String role, Integer status, int page, int size, Long userId) {
        PageHelper.startPage(page, size);
        List<OrderVO> list = tradeMapper.selectOrderPage(userId, role, status, userId);
        return PageResult.of(new PageInfo<>(list));
    }

    public OrderVO detail(String orderNo, Long userId) {
        OrderVO vo = tradeMapper.selectOrderVO(orderNo, userId);
        if (vo == null) {
            throw new BusinessException("订单不存在");
        }
        if (!vo.getBuyerId().equals(userId) && !vo.getSellerId().equals(userId)) {
            throw new BusinessException(403, "无权查看该订单");
        }
        return vo;
    }

    public List<EvaluationVO> evaluations(String orderNo, Long userId) {
        TradeOrder order = requireOrder(orderNo);
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException(403, "无权查看该订单评价");
        }
        return evaluationMapper.selectByOrderId(order.getId());
    }

    private TradeOrder requireOrder(String orderNo) {
        TradeOrder order = tradeMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    private void requireBuyer(TradeOrder order, Long userId) {
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException(403, "只有买家可以执行该操作");
        }
    }

    /** 订单号：BC + 时间戳(17) + 6 位随机，<=32 字符 */
    private String generateOrderNo() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        return "BC" + ts + String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
    }
}
