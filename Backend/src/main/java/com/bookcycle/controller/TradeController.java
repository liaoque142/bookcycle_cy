package com.bookcycle.controller;

import com.bookcycle.common.PageResult;
import com.bookcycle.common.Result;
import com.bookcycle.dto.order.CancelOrderRequest;
import com.bookcycle.dto.order.CreateEvaluationRequest;
import com.bookcycle.dto.order.CreateOrderRequest;
import com.bookcycle.dto.order.PayRequest;
import com.bookcycle.security.SecurityUtils;
import com.bookcycle.service.TradeService;
import com.bookcycle.vo.EvaluationVO;
import com.bookcycle.vo.OrderVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 交易接口：下单（防超卖）/ 模拟支付 / 发货 / 确认收货 / 取消 / 互评（均需登录）。
 */
@Validated
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    /** 下单（出售/赠送；交换二期） */
    @PostMapping
    public Result<OrderVO> create(@Valid @RequestBody CreateOrderRequest req) {
        return Result.ok(tradeService.createOrder(req, SecurityUtils.currentUserId()));
    }

    /** 订单列表：role=buyer|seller，可按状态过滤 */
    @GetMapping
    public Result<PageResult<OrderVO>> list(
            @RequestParam(defaultValue = "buyer") String role,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return Result.ok(tradeService.list(role, status, page, size, SecurityUtils.currentUserId()));
    }

    /** 订单详情（参与者可见） */
    @GetMapping("/{orderNo}")
    public Result<OrderVO> detail(@PathVariable String orderNo) {
        return Result.ok(tradeService.detail(orderNo, SecurityUtils.currentUserId()));
    }

    /** 订单评价列表（参与者可见） */
    @GetMapping("/{orderNo}/evaluations")
    public Result<List<EvaluationVO>> evaluations(@PathVariable String orderNo) {
        return Result.ok(tradeService.evaluations(orderNo, SecurityUtils.currentUserId()));
    }

    /** 模拟支付（待支付 → 待发货） */
    @PostMapping("/{orderNo}/pay")
    public Result<OrderVO> pay(@PathVariable String orderNo, @Valid @RequestBody PayRequest req) {
        return Result.ok(tradeService.pay(orderNo, req, SecurityUtils.currentUserId()));
    }

    /** 取消订单（待支付/待发货；已支付自动模拟退款，买家恶意取消扣信誉） */
    @PostMapping("/{orderNo}/cancel")
    public Result<OrderVO> cancel(@PathVariable String orderNo, @Valid @RequestBody CancelOrderRequest req) {
        return Result.ok(tradeService.cancel(orderNo, req, SecurityUtils.currentUserId()));
    }

    /** 卖家发货（待发货 → 待收货） */
    @PostMapping("/{orderNo}/ship")
    public Result<OrderVO> ship(@PathVariable String orderNo) {
        return Result.ok(tradeService.ship(orderNo, SecurityUtils.currentUserId()));
    }

    /** 买家确认收货（待收货 → 已完成，双方信誉 +2，书籍转已售出） */
    @PostMapping("/{orderNo}/confirm")
    public Result<OrderVO> confirm(@PathVariable String orderNo) {
        return Result.ok(tradeService.confirm(orderNo, SecurityUtils.currentUserId()));
    }

    /** 交易互评（完成后各评一次，好评 ≥4 分给被评方信誉 +1） */
    @PostMapping("/{orderNo}/evaluate")
    public Result<Void> evaluate(@PathVariable String orderNo, @Valid @RequestBody CreateEvaluationRequest req) {
        tradeService.evaluate(orderNo, req, SecurityUtils.currentUserId());
        return Result.ok();
    }
}
