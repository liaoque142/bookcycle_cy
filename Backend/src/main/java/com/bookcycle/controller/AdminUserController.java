package com.bookcycle.controller;

import com.bookcycle.common.PageResult;
import com.bookcycle.common.Result;
import com.bookcycle.dto.admin.AdjustCreditRequest;
import com.bookcycle.dto.admin.UserRoleRequest;
import com.bookcycle.dto.admin.UserStatusRequest;
import com.bookcycle.service.AdminUserService;
import com.bookcycle.vo.AdminUserVO;
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
 * 管理后台：用户管理接口（仅 admin 角色，SecurityConfig 已拦 /api/v1/admin/**）。
 */
@Validated
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public Result<PageResult<AdminUserVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return Result.ok(adminUserService.page(keyword, status, page, size));
    }

    @GetMapping("/{id}")
    public Result<AdminUserVO> detail(@PathVariable Long id) {
        return Result.ok(adminUserService.detail(id));
    }

    /** 启用/禁用 */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody UserStatusRequest req) {
        adminUserService.updateStatus(id, req.getStatus());
        return Result.ok();
    }

    /** 设置角色（普通账户设为管理员 / 取消管理员） */
    @PutMapping("/{id}/role")
    public Result<Void> updateRole(@PathVariable Long id, @Valid @RequestBody UserRoleRequest req) {
        adminUserService.updateRole(id, req);
        return Result.ok();
    }

    /** 调整信誉分 */
    @PutMapping("/{id}/credit")
    public Result<Void> adjustCredit(@PathVariable Long id, @Valid @RequestBody AdjustCreditRequest req) {
        adminUserService.adjustCredit(id, req);
        return Result.ok();
    }
}
