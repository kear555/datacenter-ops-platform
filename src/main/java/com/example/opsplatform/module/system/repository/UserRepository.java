package com.example.opsplatform.module.system.repository;

import com.example.opsplatform.module.system.model.UserAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * 用户与权限查询仓储。
 *
 * 第一阶段用 JdbcTemplate 写清楚 RBAC 查询链路：
 * sys_user -> sys_user_role -> sys_role -> sys_role_permission -> sys_permission。
 * 后续切换 MyBatis-Plus 时，Mapper XML 或注解 SQL 也会围绕这条链路展开。
 */
@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    /** 把 sys_user 查询结果映射成 UserAccount 对象。 */
    private final RowMapper<UserAccount> userMapper = (rs, rowNum) -> {
        UserAccount user = new UserAccount();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRealName(rs.getString("real_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setStatus(rs.getInt("status"));
        return user;
    };

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 根据用户名查询可登录账号。
     *
     * deleted = 0 表示只查未逻辑删除账号，体现 6.5D 里讲过的逻辑删除边界。
     */
    public Optional<UserAccount> findByUsername(String username) {
        List<UserAccount> users = jdbcTemplate.query(
                "select id, username, password, real_name, email, phone, status from sys_user where username = ? and deleted = 0",
                userMapper,
                username
        );
        return users.stream().findFirst();
    }

    /**
     * 查询用户拥有的权限编码。
     *
     * 这条 SQL 是 RBAC 的核心：用户不直接绑定权限，而是通过角色间接拥有权限。
     * distinct 用于避免一个用户多个角色拥有同一权限时返回重复权限编码。
     */
    public List<String> findPermissionCodes(Long userId) {
        return jdbcTemplate.queryForList(
                "select distinct p.permission_code " +
                        "from sys_user_role ur " +
                        "join sys_role r on r.id = ur.role_id and r.deleted = 0 and r.status = 1 " +
                        "join sys_role_permission rp on rp.role_id = r.id " +
                        "join sys_permission p on p.id = rp.permission_id and p.deleted = 0 " +
                        "where ur.user_id = ?",
                String.class,
                userId
        );
    }
}
