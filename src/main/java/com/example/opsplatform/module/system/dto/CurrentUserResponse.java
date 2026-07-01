package com.example.opsplatform.module.system.dto;

import java.util.Set;

/**
 * 当前登录用户响应 VO。
 *
 * /auth/me 用它返回当前 token 对应的用户信息，常用于前端刷新页面后恢复登录状态。
 */
public class CurrentUserResponse {
    private Long userId;
    private String username;
    private String realName;
    private Set<String> authorities;

    public CurrentUserResponse(Long userId, String username, String realName, Set<String> authorities) {
        this.userId = userId;
        this.username = username;
        this.realName = realName;
        this.authorities = authorities;
    }

    public Long getUserId() {
        return userId;
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
