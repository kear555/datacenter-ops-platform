package com.example.opsplatform.framework.security;

import java.util.Collections;
import java.util.Set;

/**
 * 当前登录用户对象。
 *
 * JWT 过滤器认证成功后，会把这个对象放进 SecurityContext。
 * 后续 Controller 可以通过 @AuthenticationPrincipal 直接拿到当前用户，
 * @PreAuthorize 则会使用 authorities 判断是否有接口权限。
 */
public class AuthenticatedUser {
    private final Long id;
    private final String username;
    private final String realName;
    private final Set<String> authorities;

    public AuthenticatedUser(Long id, String username, String realName, Set<String> authorities) {
        this.id = id;
        this.username = username;
        this.realName = realName;
        this.authorities = authorities == null ? Collections.emptySet() : Collections.unmodifiableSet(authorities);
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRealName() {
        return realName;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }
}
