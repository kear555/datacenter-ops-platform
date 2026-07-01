package com.example.opsplatform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 登录鉴权最小闭环测试。
 *
 * 这组测试就是第一阶段的自动化验收证据：
 * 1. 不带 token 访问 /auth/me，必须返回 401。
 * 2. admin 使用正确密码登录，必须返回 token 和权限列表。
 * 3. 携带 admin token 访问 /auth/me，必须能恢复当前用户。
 * 4. viewer 已登录但没有 task:execute，调用任务执行接口必须返回 403。
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturn401WhenRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40001));
    }

    @Test
    void shouldLoginAndReadCurrentUser() throws Exception {
        String token = loginAndGetToken("admin", "123456");

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.authorities").isArray());
    }

    @Test
    void shouldReturn403WhenViewerExecutesTask() throws Exception {
        String token = loginAndGetToken("viewer", "123456");

        mockMvc.perform(post("/api/v1/tasks/1001/mock-execute")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40003));
    }

    @Test
    void shouldAllowAdminToExecuteTask() throws Exception {
        String token = loginAndGetToken("admin", "123456");

        mockMvc.perform(post("/api/v1/tasks/1001/mock-execute")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("MOCK_EXECUTED"));
    }

    /**
     * 测试辅助方法：登录并从统一响应体 data.accessToken 中取 token。
     */
    private String loginAndGetToken(String username, String password) throws Exception {
        String body = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("accessToken").asText();
    }
}
