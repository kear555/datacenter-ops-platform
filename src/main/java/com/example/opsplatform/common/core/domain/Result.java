package com.example.opsplatform.common.core.domain;

import com.example.opsplatform.common.enums.ErrorCode;

/**
 * 统一响应体。
 *
 * 这个类是所有 Controller 返回给前端的固定结构：
 * - code：业务码，成功固定为 0。
 * - message：给前端或调用方看的简短提示。
 * - data：真正的业务数据。
 * - traceId：一次请求的追踪编号，出问题时前端把它给后端，后端就能按日志链路排查。
 */
public class Result<T> {
    private int code;
    private String message;
    private T data;
    private String traceId;

    /**
     * 成功响应的工厂方法，避免每个 Controller 手动 new Result。
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = ErrorCode.SUCCESS.getCode();
        result.message = ErrorCode.SUCCESS.getMessage();
        result.data = data;
        return result;
    }

    /**
     * 失败响应的工厂方法，统一从 ErrorCode 取业务码和默认提示。
     */
    public static <T> Result<T> fail(ErrorCode errorCode) {
        return fail(errorCode, errorCode.getMessage());
    }

    /**
     * 失败响应的工厂方法，允许覆盖默认提示，例如参数校验失败时返回具体字段原因。
     */
    public static <T> Result<T> fail(ErrorCode errorCode, String message) {
        Result<T> result = new Result<>();
        result.code = errorCode.getCode();
        result.message = message;
        return result;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
