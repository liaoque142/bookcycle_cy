package com.bookcycle.exception;

import lombok.Getter;

/**
 * 业务异常：Controller 不 catch，统一交给 GlobalExceptionHandler 转 Result。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
