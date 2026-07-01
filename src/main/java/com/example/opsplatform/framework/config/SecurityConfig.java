package com.example.opsplatform.framework.config;

import com.example.opsplatform.common.enums.ErrorCode;
import com.example.opsplatform.common.core.domain.Result;
import com.example.opsplatform.framework.security.JwtAuthenticationFilter;
import com.example.opsplatform.framework.web.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 核心配置。
 *
 * 这份配置解决三个问题：
 * 1. 哪些接口不用登录：登录接口、H2 控制台。
 * 2. 哪些接口必须登录：除白名单外的所有接口。
 * 3. 未登录和无权限怎么返回统一 JSON：401 / 403 都走 Result 结构，并带 traceId。
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                // H2 控制台需要 frame，同源放开即可。本地教学使用，生产环境不要暴露 H2 console。
                .headers().frameOptions().sameOrigin()
                .and()
                // JWT 是无状态认证，服务端不保存 session。
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // 登录接口必须放行，否则用户还没 token 就无法登录。
                .antMatchers("/api/v1/auth/login", "/h2-console/**").permitAll()
                // 其他接口都需要先通过 JWT 认证。
                .anyRequest().authenticated()
                .and()
                .exceptionHandling()
                // 未登录、token 缺失、token 无效：返回 HTTP 401 + 业务码 40001。
                .authenticationEntryPoint((request, response, exception) -> writeError(response, HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED))
                // 已登录但权限不够：返回 HTTP 403 + 业务码 40003。
                .accessDeniedHandler((request, response, exception) -> writeError(response, HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN))
                .and()
                // JWT 过滤器必须放在 UsernamePasswordAuthenticationFilter 前面，先从请求头恢复当前用户。
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 密码编码器。
     *
     * 使用 Spring Security 推荐的 DelegatingPasswordEncoder，数据库密码前缀 {bcrypt}
     * 会自动选择 BCrypt 校验器。这样既安全，也方便后续升级算法。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Security 异常不经过 @RestControllerAdvice，所以这里手动写统一 JSON 响应。
     */
    private void writeError(javax.servlet.http.HttpServletResponse response, HttpStatus status, ErrorCode errorCode)
            throws java.io.IOException {
        Result<Void> result = Result.fail(errorCode);
        GlobalExceptionHandler.fillTraceId(result);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), result);
    }
}
