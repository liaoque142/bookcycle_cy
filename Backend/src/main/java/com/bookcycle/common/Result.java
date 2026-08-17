package com.bookcycle.common;

import lombok.Data;

/**
 * 统一响应体（plan.md §8：{ code, message, data }）。
 * code：200 成功 / 400 参数或业务错误 / 401 未认证 / 403 无权限 / 500 服务端错误。
 */
@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = "操作成功";
        r.data = data;
        return r;
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }

    public static <T> Result<T> fail(String message) {
        return fail(400, message);
    }
}
