package com.bookcycle.controller;

import com.bookcycle.common.Result;
import com.bookcycle.dto.auth.LoginRequest;
import com.bookcycle.dto.auth.LoginResultVO;
import com.bookcycle.dto.auth.RegisterRequest;
import com.bookcycle.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：注册 / 登录 / 登出。
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return Result.ok();
    }

    @PostMapping("/login")
    public Result<LoginResultVO> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(authService.login(req));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        // JWT 无状态：前端清除本地 token 即可。Token 黑名单待 Redis 安装后启用（plan.md 风险表降级方案）
        return Result.ok();
    }
}
