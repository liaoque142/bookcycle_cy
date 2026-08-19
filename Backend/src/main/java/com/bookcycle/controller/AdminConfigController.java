package com.bookcycle.controller;

import com.bookcycle.common.Result;
import com.bookcycle.dto.admin.UpdateCreditRuleStatusRequest;
import com.bookcycle.service.AdminConfigService;
import com.bookcycle.vo.CreditRuleVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理后台：系统配置接口（信誉规则启用/禁用，仅 admin 角色）。
 */
@RestController
@RequestMapping("/api/v1/admin/credit-rules")
@RequiredArgsConstructor
public class AdminConfigController {

    private final AdminConfigService adminConfigService;

    @GetMapping
    public Result<List<CreditRuleVO>> rules() {
        return Result.ok(adminConfigService.rules());
    }

    /** 启用（status=1）/ 禁用（status=0）信誉规则 */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateCreditRuleStatusRequest req) {
        adminConfigService.updateStatus(id, req.getStatus());
        return Result.ok();
    }
}
