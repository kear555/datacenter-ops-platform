package com.example.opsplatform.module.system.service;

import com.example.opsplatform.common.exception.BusinessException;
import com.example.opsplatform.common.enums.ErrorCode;
import com.example.opsplatform.module.system.dto.LoginRequest;
import com.example.opsplatform.module.system.dto.LoginResponse;
import com.example.opsplatform.module.system.model.UserAccount;
import com.example.opsplatform.module.system.repository.UserRepository;
import com.example.opsplatform.framework.security.AuthenticatedUser;
import com.example.opsplatform.framework.security.JwtTokenProvider;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 登录认证业务服务。
 *
 * 这里对应 6.5A 的登录链路：
 * 1. 根据用户名查询账号。
 * 2. 判断账号是否启用。
 * 3. 使用 BCrypt 校验密码。
 * 4. 查询用户通过角色拥有的权限编码。
 * 5. 生成 JWT 返回给前端。
 */
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    /**
     * 执行登录。
     *
     * 安全设计：
     * - 用户不存在和密码错误都返回 LOGIN_FAILED，不告诉前端到底是哪一种，减少账号枚举风险。
     * - 密码只和数据库中的 BCrypt 密文比较，不做明文存储。
     * - 登录成功后把权限编码放入 JWT，后续接口用 @PreAuthorize 判断。
     */
    public LoginResponse login(LoginRequest request) {
        UserAccount user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        Set<String> authorities = userRepository.findPermissionCodes(user.getId()).stream().collect(Collectors.toSet());
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(user.getId(), user.getUsername(), user.getRealName(), authorities);
        String token = tokenProvider.createToken(authenticatedUser);

        LoginResponse response = new LoginResponse();
        response.setAccessToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setAuthorities(authorities);
        return response;
    }
}
