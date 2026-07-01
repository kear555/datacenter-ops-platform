package com.example.opsplatform.framework.web.exception;

import com.example.opsplatform.common.core.domain.Result;
import com.example.opsplatform.common.enums.ErrorCode;
import com.example.opsplatform.common.exception.BusinessException;
import com.example.opsplatform.framework.web.filter.TraceIdFilter;
import java.util.stream.Collectors;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 *
 * 它对应第 6 章的“异常出口统一包装”：
 * Controller 和 Service 可以专注业务逻辑，异常最终统一在这里变成固定 Result 结构。
 * 注意：未知异常只返回友好提示和 traceId，不能把原始异常 message、SQL、堆栈暴露给前端。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常，例如用户名密码错误、账号禁用、资源不存在。
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException exception) {
        Result<Void> result = Result.fail(exception.getErrorCode(), exception.getMessage());
        fillTraceId(result);
        HttpStatus status = exception.getErrorCode() == ErrorCode.LOGIN_FAILED ? HttpStatus.UNAUTHORIZED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(result);
    }

    /**
     * 处理方法级授权异常。
     *
     * 为什么这里还要处理 403：
     * - URL 级别的无权限通常会被 Spring Security 的 AccessDeniedHandler 接住。
     * - @PreAuthorize 属于方法级授权，异常可能已经进入 MVC 调用链，再由全局异常处理器接住。
     * - 如果不单独处理，它会落入 Exception 兜底，错误地变成 500。
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleAccessDeniedException(AccessDeniedException exception) {
        Result<Void> result = Result.fail(ErrorCode.FORBIDDEN);
        fillTraceId(result);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
    }

    /**
     * 处理参数校验异常，例如 @NotBlank、@Size、@Valid 校验失败。
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<Result<Void>> handleValidationException(Exception exception) {
        String message;
        if (exception instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException validException = (MethodArgumentNotValidException) exception;
            message = validException.getBindingResult().getFieldErrors().stream()
                    .map(error -> error.getField() + " " + error.getDefaultMessage())
                    .collect(Collectors.joining("; "));
        } else {
            BindException bindException = (BindException) exception;
            message = bindException.getBindingResult().getFieldErrors().stream()
                    .map(error -> error.getField() + " " + error.getDefaultMessage())
                    .collect(Collectors.joining("; "));
        }
        Result<Void> result = Result.fail(ErrorCode.PARAM_ERROR, message);
        fillTraceId(result);
        return ResponseEntity.badRequest().body(result);
    }

    /**
     * 兜底未知异常。
     *
     * 真实项目中这里还会 log.error 打完整堆栈；当前第一步先保证响应结构正确。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception exception) {
        // TODO(human): 在这里补充日志记录（约3行）
        // 要求：用 SLF4J 的 Logger 打印 error 级别日志
        // 日志信息需要包含 traceId（从 MDC 中取）和完整异常堆栈
        // 参考：MDC.get(TraceIdFilter.TRACE_ID) 可以拿到当前 traceId
        Result<Void> result = Result.fail(ErrorCode.INTERNAL_ERROR);
        fillTraceId(result);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    /**
     * 从 MDC 取当前请求 traceId，填入响应体。
     */
    public static void fillTraceId(Result<?> result) {
        result.setTraceId(MDC.get(TraceIdFilter.TRACE_ID));
    }
}
