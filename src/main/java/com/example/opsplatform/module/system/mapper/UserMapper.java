package com.example.opsplatform.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.opsplatform.module.system.model.UserAccount;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户数据访问层。
 *
 * 继承 BaseMapper<UserAccount> 后，MyBatis-Plus 自动提供：
 * selectById / selectOne / insert / updateById / deleteById 等通用方法。
 * 其中 select 类方法会自动拼接 deleted=0 条件（基于 @TableLogic）。
 *
 * findPermissionCodes 是自定义 SQL，用于 RBAC 权限链路查询。
 */
@Mapper
public interface UserMapper extends BaseMapper<UserAccount> {

    /**
     * 查询用户通过角色拥有的所有权限编码。
     *
     * 用户不直接绑定权限，通过"用户->角色->权限"三级链路间接持有。
     * distinct 避免一个用户多角色拥有同一权限时返回重复编码。
     */
    @Select("SELECT DISTINCT p.permission_code " +
            "FROM sys_user_role ur " +
            "JOIN sys_role r ON r.id = ur.role_id AND r.deleted = 0 AND r.status = 1 " +
            "JOIN sys_role_permission rp ON rp.role_id = r.id " +
            "JOIN sys_permission p ON p.id = rp.permission_id AND p.deleted = 0 " +
            "WHERE ur.user_id = #{userId}")
    List<String> findPermissionCodes(@Param("userId") Long userId);
}
