package com.example.opsplatform.common.enums;

/**
 * 统一业务错误码。
 *
 * 这一类对应第 6 章“统一异常 + 统一返回体”的落地：
 * 1. HTTP 状态码表达协议层结果，例如 401、403、500。
 * 2. code 表达业务层结果，例如登录失败、无权限、参数错误。
 * 3. 前端、Apifox 验收、面试表达都围绕这套稳定错误码来判断接口行为。
 */
public enum ErrorCode {
    SUCCESS(0, "success"),
    PARAM_ERROR(10001, "Invalid request parameter"),
    UNAUTHORIZED(40001, "Authentication required"),
    LOGIN_FAILED(40002, "Invalid username or password"),
    FORBIDDEN(40003, "Access denied"),
    ACCOUNT_DISABLED(40004, "Account is disabled"),
    INTERNAL_ERROR(50000, "Internal server error");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
