package com.bookcycle.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求：账号为手机号或邮箱。
 */
@Data
public class LoginRequest {

    @NotBlank(message = "请输入手机号或邮箱")
    private String account;

    @NotBlank(message = "请输入密码")
    private String password;
}
