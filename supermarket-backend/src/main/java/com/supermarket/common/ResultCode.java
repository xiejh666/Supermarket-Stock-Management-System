package com.supermarket.common;

import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
public enum ResultCode {
    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 失败
     */
    ERROR(500, "操作失败"),

    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权"),

    /**
     * 禁止访问
     */
    FORBIDDEN(403, "禁止访问"),

    /**
     * 未找到
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 用户名或密码错误
     */
    LOGIN_ERROR(401, "用户名或密码错误"),

    /**
     * Token已过期
     */
    TOKEN_EXPIRED(401, "Token已过期"),

    /**
     * Token无效
     */
    TOKEN_INVALID(401, "Token无效");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}

