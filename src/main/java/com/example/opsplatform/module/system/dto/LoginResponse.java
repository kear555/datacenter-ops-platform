package com.example.opsplatform.module.system.dto;

import java.util.Set;

/**
 * 登录成功响应 VO。
 *
 * 返回 token 的同时，也返回当前用户基础信息和权限列表，方便前端决定菜单或按钮是否展示。
 */
public class LoginResponse {
    /** token 类型，前端后续请求头格式为 Authorization: Bearer <accessToken>。 */
    private String tokenType = "Bearer";
    private String accessToken;
    private Long userId;
    private String username;
    private String realName;
    private Set<String> authorities;

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
    }
}
