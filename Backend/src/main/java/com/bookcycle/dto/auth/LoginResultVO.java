package com.bookcycle.dto.auth;

import com.bookcycle.vo.UserVO;
import lombok.Builder;
import lombok.Data;

/**
 * 登录结果：token + 用户信息（对齐前端 LoginResult 契约）。
 */
@Data
@Builder
public class LoginResultVO {

    private String token;
    private UserVO user;
}
