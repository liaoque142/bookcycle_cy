package com.bookcycle.security;

import com.bookcycle.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户工具：JWT filter 已把 userId 写入 principal。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /** 当前登录用户 ID（未登录抛 401 业务异常） */
    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            return userId;
        }
        throw new BusinessException(401, "未登录");
    }
}
