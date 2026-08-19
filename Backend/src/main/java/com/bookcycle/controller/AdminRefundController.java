package com.bookcycle.controller;

import com.bookcycle.common.PageResult;
import com.bookcycle.common.Result;
import com.bookcycle.dto.admin.HandleRefundRequest;
import com.bookcycle.service.AdminRefundService;
import com.bookcycle.vo.RefundVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理后台：售后/退款接口（仅 admin 角色）。
 */
@Validated
@RestController
@RequestMapping("/api/v1/admin/refunds")
@RequiredArgsConstructor
public class AdminRefundController {

    private final AdminRefundService adminRefundService;

    /** 售后列表（keyword 匹配订单号/书名/申请人用户名，可按状态/类型筛选） */
    @GetMapping
    public Result<PageResult<RefundVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return Result.ok(adminRefundService.page(keyword, status, type, page, size));
    }

    /** 处理售后：agree=true 同意退款（订单取消 + 释放书籍 + 原路退款），false 拒绝 */
    @PutMapping("/{id}/handle")
    public Result<Void> handle(@PathVariable Long id, @Valid @RequestBody HandleRefundRequest req) {
        adminRefundService.handle(id, req);
        return Result.ok();
    }
}
