package com.example.opsplatform.module.system.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 系统用户实体。
 *
 * 已从第一阶段的 JDBC RowMapper 承载对象升级为 MyBatis-Plus Entity：
 * - @TableName：指定对应的数据库表名。
 * - @TableId：标记主键字段，IdType.INPUT 表示由调用方手动指定 id 值。
 * - @TableLogic：标记逻辑删除字段，MyBatis-Plus 会在所有查询中自动追加 deleted=0 条件。
 */
@TableName("sys_user")
public class UserAccount {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String username;

    private String password;

    private String realName;

    private String email;

    private String phone;

    private Integer status;

    /** MyBatis-Plus 逻辑删除字段，全局配置已设 logic-delete-field=deleted，此处显式标注增强可读性。 */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}
