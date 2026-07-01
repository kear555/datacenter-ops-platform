package com.example.opsplatform.framework.security;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT 认证过滤器。
 *
 * 它对应 6.5A 里讲过的“过滤器负责认证”：
 * 1. 从请求头 Authorization 中取 Bearer token。
 * 2. 校验 token 签名和过期时间。
 * 3. 把 token 中的用户信息和权限转换成 Spring Security 认识的 Authentication。
 * 4. 放入 SecurityContext，后续 Controller 和 @PreAuthorize 才知道当前用户是谁。
 *
 * 注意：这个过滤器只做“认证”，不直接判断某个接口能不能访问；接口级授权交给 @PreAuthorize。
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Map<String, Object> payload = tokenProvider.parseToken(token);
                AuthenticatedUser user = toUser(payload);

                // Spring Security 的授权判断需要 GrantedAuthority，这里把权限编码转成 SimpleGrantedAuthority。
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet())
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (IllegalArgumentException ignored) {
                // token 无效时不在这里直接写响应，让后续 Security 统一返回 401。
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 把 JWT payload 还原成当前登录用户对象。
     */
    @SuppressWarnings("unchecked")
    private AuthenticatedUser toUser(Map<String, Object> payload) {
        Number uid = (Number) payload.get("uid");
        String username = String.valueOf(payload.get("sub"));
        String realName = String.valueOf(payload.get("name"));
        Collection<String> auth = (Collection<String>) payload.get("auth");
        Set<String> authorities = auth == null ? Set.of() : Set.copyOf(auth);
        return new AuthenticatedUser(uid.longValue(), username, realName, authorities);
    }
}
