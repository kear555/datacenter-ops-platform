package com.example.opsplatform.framework.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT 令牌工具类。
 *
 * 教学说明：
 * 1. 这里没有引入额外 JWT 三方库，而是用 JDK HMAC-SHA256 手写最小版，方便你看清楚 JWT 的三段结构。
 * 2. JWT = header.payload.signature。
 * 3. header 说明签名算法，payload 放用户身份和权限，signature 用 secret 对前两段签名。
 * 4. 后端校验 JWT 时必须验证签名和过期时间，不能只把 payload Base64 解出来就信任。
 * 5. 生产项目可以换成成熟 JWT 库，但理解这条链路比记库 API 更重要。
 */
@Component
public class JwtTokenProvider {
    private static final String HMAC_SHA256 = "HmacSHA256";

    /** Jackson 用于把 header/payload 转成 JSON 字节。 */
    private final ObjectMapper objectMapper;

    /** JWT 签名密钥。生产环境必须从环境变量或配置中心注入，不能硬编码。 */
    private final byte[] secret;

    /** token 有效期，单位秒。 */
    private final long expireSeconds;

    public JwtTokenProvider(ObjectMapper objectMapper,
                            @Value("${app.jwt.secret}") String secret,
                            @Value("${app.jwt.expire-minutes}") long expireMinutes) {
        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expireSeconds = expireMinutes * 60;
    }

    /**
     * 登录成功后生成 JWT。
     *
     * payload 中放入：
     * - sub：用户名，JWT 里常用 sub 表示主体。
     * - uid：用户 id，后续操作日志可以知道是谁操作。
     * - name：展示名，方便 /auth/me 返回。
     * - auth：权限编码集合，后续 @PreAuthorize 用它判断授权。
     * - iat / exp：签发时间和过期时间。
     */
    public String createToken(AuthenticatedUser user) {
        long now = Instant.now().getEpochSecond();
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", user.getUsername());
        payload.put("uid", user.getId());
        payload.put("name", user.getRealName());
        payload.put("auth", user.getAuthorities());
        payload.put("iat", now);
        payload.put("exp", now + expireSeconds);

        String headerPart = encodeJson(header);
        String payloadPart = encodeJson(payload);
        String content = headerPart + "." + payloadPart;
        return content + "." + sign(content);
    }

    /**
     * 解析并校验 JWT。
     *
     * 安全重点：
     * 1. 必须是三段式 token。
     * 2. 必须重新计算签名并比较，防止前端伪造权限。
     * 3. 必须检查 exp，过期 token 不能继续使用。
     */
    public Map<String, Object> parseToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid token structure");
        }
        String content = parts[0] + "." + parts[1];
        if (!constantTimeEquals(sign(content), parts[2])) {
            throw new IllegalArgumentException("Invalid token signature");
        }
        try {
            Map<String, Object> payload = objectMapper.readValue(Base64.getUrlDecoder().decode(parts[1]), new TypeReference<Map<String, Object>>() {});
            Number exp = (Number) payload.get("exp");
            if (exp == null || exp.longValue() < Instant.now().getEpochSecond()) {
                throw new IllegalArgumentException("Token expired");
            }
            return payload;
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid token payload", exception);
        }
    }

    /** 把 JSON 对象转成 Base64URL 字符串，符合 JWT 标准格式。 */
    private String encodeJson(Map<String, Object> json) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(json);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to encode JWT", exception);
        }
    }

    /** 对 header.payload 做 HMAC-SHA256 签名。 */
    private String sign(String content) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret, HMAC_SHA256));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to sign JWT", exception);
        }
    }

    /**
     * 固定时间比较签名，避免普通字符串比较在安全场景下暴露过多时序信息。
     * 这属于安全工程细节，第一阶段知道它是“更稳妥的签名比较方式”即可。
     */
    private boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null || left.length() != right.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < left.length(); i++) {
            result |= left.charAt(i) ^ right.charAt(i);
        }
        return result == 0;
    }
}
