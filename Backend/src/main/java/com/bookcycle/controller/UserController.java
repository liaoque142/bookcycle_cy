package com.bookcycle.controller;

import com.bookcycle.common.Result;
import com.bookcycle.dto.user.UpdateProfileRequest;
import com.bookcycle.security.SecurityUtils;
import com.bookcycle.service.UserService;
import com.bookcycle.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户资料接口：我的资料获取 / 更新（均需登录）。
 */
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 我的资料（含简介、兴趣标签、书籍喜好） */
    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.ok(userService.me(SecurityUtils.currentUserId()));
    }

    /** 更新我的资料（头像/昵称/简介/兴趣标签/书籍喜好，返回最新 UserVO） */
    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@Valid @RequestBody UpdateProfileRequest req) {
        return Result.ok(userService.updateProfile(SecurityUtils.currentUserId(), req));
    }
}
