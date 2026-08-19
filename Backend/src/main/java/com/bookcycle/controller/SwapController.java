package com.bookcycle.controller;

import com.bookcycle.common.PageResult;
import com.bookcycle.common.Result;
import com.bookcycle.dto.swap.AcceptSwapRequest;
import com.bookcycle.dto.swap.CancelSwapRequest;
import com.bookcycle.dto.swap.CreateSwapRequest;
import com.bookcycle.security.SecurityUtils;
import com.bookcycle.service.SwapService;
import com.bookcycle.vo.SwapVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 交换接口（均需登录）：发起 / 列表 / 详情 / 同意 / 拒绝 / 取消 / 发货 / 确认收货。
 */
@Validated
@RestController
@RequestMapping("/api/v1/swaps")
@RequiredArgsConstructor
public class SwapController {

    private final SwapService swapService;

    /** 发起交换：用我的书 B 换卖家书 A */
    @PostMapping
    public Result<SwapVO> create(@Valid @RequestBody CreateSwapRequest req) {
        return Result.ok(swapService.create(req, SecurityUtils.currentUserId()));
    }

    /** 我的交换列表：role=initiator|receiver，可按状态过滤 */
    @GetMapping
    public Result<PageResult<SwapVO>> list(
            @RequestParam(defaultValue = "initiator") String role,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return Result.ok(swapService.list(role, status, page, size, SecurityUtils.currentUserId()));
    }

    /** 交换单详情（参与者可见） */
    @GetMapping("/{swapNo}")
    public Result<SwapVO> detail(@PathVariable String swapNo) {
        return Result.ok(swapService.detail(swapNo, SecurityUtils.currentUserId()));
    }

    /** 接收方同意（填写收书地址） */
    @PutMapping("/{swapNo}/accept")
    public Result<SwapVO> accept(@PathVariable String swapNo, @Valid @RequestBody AcceptSwapRequest req) {
        return Result.ok(swapService.accept(swapNo, req, SecurityUtils.currentUserId()));
    }

    /** 接收方拒绝 */
    @PutMapping("/{swapNo}/reject")
    public Result<SwapVO> reject(@PathVariable String swapNo) {
        return Result.ok(swapService.reject(swapNo, SecurityUtils.currentUserId()));
    }

    /** 取消（待对方同意/已同意待发货，本人未发货可取消） */
    @PostMapping("/{swapNo}/cancel")
    public Result<SwapVO> cancel(@PathVariable String swapNo, @Valid @RequestBody CancelSwapRequest req) {
        return Result.ok(swapService.cancel(swapNo, req, SecurityUtils.currentUserId()));
    }

    /** 发货（本人发自己的书） */
    @PostMapping("/{swapNo}/ship")
    public Result<SwapVO> ship(@PathVariable String swapNo) {
        return Result.ok(swapService.ship(swapNo, SecurityUtils.currentUserId()));
    }

    /** 确认收货（需对方已发货） */
    @PostMapping("/{swapNo}/confirm")
    public Result<SwapVO> confirm(@PathVariable String swapNo) {
        return Result.ok(swapService.confirm(swapNo, SecurityUtils.currentUserId()));
    }
}
