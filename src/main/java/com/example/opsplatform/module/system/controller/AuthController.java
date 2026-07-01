package com.example.opsplatform.module.system.controller;

import com.example.opsplatform.common.core.domain.Result;
import com.example.opsplatform.module.system.dto.CurrentUserResponse;
import com.example.opsplatform.module.system.dto.LoginRequest;
import com.example.opsplatform.module.system.dto.LoginResponse;
import com.example.opsplatform.module.system.service.AuthService;
import com.example.opsplatform.framework.security.AuthenticatedUser;
import javax.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口。
 *
 * 当前只做两个最小闭环接口：
 * - POST /api/v1/auth/login：用户名密码登录，返回 JWT。
 * - GET /api/v1/auth/me：携带 JWT 查询当前登录用户，验证 token 是否能恢复用户身份。
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 登录接口。
     *
     * @Valid 会触发 LoginRequest 中的 @NotBlank 校验；
     * 参数错误会被 GlobalExceptionHandler 统一转换成 400 + Result。
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    /**
     * 当前用户接口。
     *
     * @AuthenticationPrincipal 从 SecurityContext 取出 JwtAuthenticationFilter 放入的 AuthenticatedUser。
     * 如果没有携带 token，请求会先被 Security 拦截并返回 401，不会进入这个方法。
     */
    @GetMapping("/me")
    public Result<CurrentUserResponse> me(@AuthenticationPrincipal AuthenticatedUser user) {
        return Result.success(new CurrentUserResponse(user.getId(), user.getUsername(), user.getRealName(), user.getAuthorities()));
    }
}
