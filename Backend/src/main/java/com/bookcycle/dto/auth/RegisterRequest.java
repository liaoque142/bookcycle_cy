package com.bookcycle.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求：账号为手机号或邮箱，昵称选填。
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "请输入手机号或邮箱")
    private String account;

    @NotBlank(message = "请输入密码")
    @Size(min = 6, max = 64, message = "密码长度需在 6-64 位之间")
    private String password;

    @Size(max = 20, message = "昵称最多 20 个字符")
    private String nickname;
}
