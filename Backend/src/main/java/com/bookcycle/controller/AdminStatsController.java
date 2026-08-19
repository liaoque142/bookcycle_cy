package com.bookcycle.controller;

import com.bookcycle.common.Result;
import com.bookcycle.service.AdminStatsService;
import com.bookcycle.vo.StatsOverviewVO;
import com.bookcycle.vo.StatsTrendVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理后台：数据统计接口（仅 admin 角色）。
 */
@Validated
@RestController
@RequestMapping("/api/v1/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    /** 概览指标（用户/书籍/订单/今日/交易额/待处理售后） */
    @GetMapping("/overview")
    public Result<StatsOverviewVO> overview() {
        return Result.ok(adminStatsService.overview());
    }

    /** 近 N 天每日订单/新增用户趋势（默认 7 天） */
    @GetMapping("/trend")
    public Result<List<StatsTrendVO>> trend(@RequestParam(defaultValue = "7") @Min(1) @Max(30) int days) {
        return Result.ok(adminStatsService.trend(days));
    }
}
