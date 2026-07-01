package com.example.opsplatform.framework.web.advice;

import com.example.opsplatform.common.core.domain.Result;
import com.example.opsplatform.framework.web.exception.GlobalExceptionHandler;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 响应体增强器。
 *
 * Controller 正常返回 Result.success(...) 时，业务代码不需要手动塞 traceId。
 * 这个 Advice 会在响应写出前统一补 traceId，保证成功和失败响应都有追踪编号。
 */
@ControllerAdvice
public class ResultTraceAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof Result) {
            GlobalExceptionHandler.fillTraceId((Result<?>) body);
        }
        return body;
    }
}
