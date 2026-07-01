package com.example.opsplatform.framework.web.filter;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * traceId 过滤器。
 *
 * 它解决第 6.5C 的问题：接口报错后，前端不能看到堆栈、SQL、数据库地址等敏感信息，
 * 但必须拿到一个 traceId。后端日志里也带同一个 traceId，排查时就能串起完整请求链路。
 */
@Component
public class TraceIdFilter extends OncePerRequestFilter {
    public static final String TRACE_ID = "traceId";
    public static final String TRACE_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 如果上游网关或调用方已经传了 traceId，就继续沿用；否则本服务生成一个新的。
        String traceId = request.getHeader(TRACE_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        // MDC 是日志上下文。后续日志格式如果打印 %X{traceId}，就会自动带上这个值。
        MDC.put(TRACE_ID, traceId);
        response.setHeader(TRACE_HEADER, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            // 请求线程会被线程池复用，请求结束必须清理，避免下一个请求串到旧 traceId。
            MDC.remove(TRACE_ID);
        }
    }
}
