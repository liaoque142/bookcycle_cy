package com.bookcycle.controller;

import com.bookcycle.common.PageResult;
import com.bookcycle.common.Result;
import com.bookcycle.security.SecurityUtils;
import com.bookcycle.service.CreditService;
import com.bookcycle.vo.CreditLogVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 信誉接口：当前信誉分 + 变动日志（需登录）。
 */
@Validated
@RestController
@RequestMapping("/api/v1/credit")
@RequiredArgsConstructor
public class CreditController {

    private final CreditService creditService;

    @GetMapping("/me")
    public Result<Integer> me() {
        return Result.ok(creditService.current(SecurityUtils.currentUserId()));
    }

    @GetMapping("/logs")
    public Result<PageResult<CreditLogVO>> logs(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return Result.ok(creditService.logs(SecurityUtils.currentUserId(), page, size));
    }
}
