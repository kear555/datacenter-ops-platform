package com.example.opsplatform.module.system.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.opsplatform.module.system.mapper.UserMapper;
import com.example.opsplatform.module.system.model.UserAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * 用户与权限查询仓储。
 *
 * 已从第一阶段的 JdbcTemplate 迁移到 MyBatis-Plus：
 * - findByUsername：改用 LambdaQueryWrapper，字段引用类型安全，无手写 SQL。
 * - findPermissionCodes：委托给 UserMapper 的自定义 @Select，RBAC 链路不变。
 * - @TableLogic 已在 UserAccount 上声明，所有查询自动追加 deleted=0，无需手写。
 */
@Repository
public class UserRepository {

    private final UserMapper userMapper;

    public UserRepository(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 根据用户名查询可登录账号。
     *
     * LambdaQueryWrapper 使用方法引用代替字符串字段名，重命名字段时编译即报错，
     * 比 JdbcTemplate 手写 SQL 更安全。
     * deleted=0 条件由 @TableLogic 自动拼接，无需显式写。
     */
    public Optional<UserAccount> findByUsername(String username) {
        UserAccount user = userMapper.selectOne(
                new LambdaQueryWrapper<UserAccount>()
                        .eq(UserAccount::getUsername, username)
        );
        return Optional.ofNullable(user);
    }

    /**
     * 查询用户拥有的权限编码（通过角色间接持有）。
     */
    public List<String> findPermissionCodes(Long userId) {
        return userMapper.findPermissionCodes(userId);
    }
}
