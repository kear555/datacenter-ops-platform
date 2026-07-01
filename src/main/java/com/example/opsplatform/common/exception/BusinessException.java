package com.example.opsplatform.common.exception;

import com.example.opsplatform.common.enums.ErrorCode;

/**
 * 业务异常。
 *
 * Service 层发现“用户名密码错误、账号禁用、资源不存在”等可预期业务失败时，
 * 抛出这个异常，由 GlobalExceptionHandler 统一转换成 Result。
 * 这样 Controller 不需要到处 try-catch，也不会把原始异常信息直接暴露给前端。
 */
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
