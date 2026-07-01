package com.example.opsplatform.module.system.dto;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求 DTO。
 *
 * DTO 只接收接口入参，不直接复用 sys_user 实体，避免前端传入 status、deleted、role 等不该由登录接口控制的字段。
 */
public class LoginRequest {
    /** 用户名不能为空，对应第 5 章参数校验。 */
    @NotBlank(message = "不能为空")
    private String username;

    /** 密码不能为空；密码是否正确由 Service 使用 BCrypt 校验。 */
    @NotBlank(message = "不能为空")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
