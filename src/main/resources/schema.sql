-- ============================================================
-- 数据中心智能运维支撑平台 - RBAC 表结构
-- ============================================================
-- 说明：
-- 1. 已切换到 MySQL，使用标准 MySQL 语法（InnoDB + utf8mb4）。
-- 2. 第一阶段只建 RBAC 相关的 5 张表，资源台账表在后续模块追加。
-- 3. spring.sql.init.mode=always，每次启动都会重新执行，适合本地开发。

-- 先删关联表，再删主表（避免外键或逻辑依赖的顺序问题）
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;

-- ============================================================
-- sys_user：系统用户表
-- ============================================================
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL COMMENT '主键',
    username    VARCHAR(64)  NOT NULL COMMENT '登录名，全局唯一',
    password    VARCHAR(120) NOT NULL COMMENT 'BCrypt 密文，不保存明文',
    real_name   VARCHAR(64)  NOT NULL COMMENT '真实姓名/展示名',
    email       VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    phone       VARCHAR(32)  DEFAULT NULL COMMENT '手机号',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '账号状态：1-启用 0-禁用',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username),
    UNIQUE KEY uk_sys_user_email (email),
    UNIQUE KEY uk_sys_user_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ============================================================
-- sys_role：角色表
-- ============================================================
CREATE TABLE sys_role (
    id          BIGINT      NOT NULL COMMENT '主键',
    role_code   VARCHAR(64) NOT NULL COMMENT '角色编码，稳定标识',
    role_name   VARCHAR(64) NOT NULL COMMENT '角色展示名',
    status      TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    deleted     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ============================================================
-- sys_permission：权限表
-- ============================================================
CREATE TABLE sys_permission (
    id              BIGINT       NOT NULL COMMENT '主键',
    permission_code VARCHAR(128) NOT NULL COMMENT '权限编码，如 task:execute',
    permission_name VARCHAR(128) NOT NULL COMMENT '权限展示名',
    module_name     VARCHAR(64)  NOT NULL COMMENT '所属模块',
    deleted         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- ============================================================
-- sys_user_role：用户角色关联表
-- ============================================================
CREATE TABLE sys_user_role (
    id          BIGINT   NOT NULL COMMENT '主键',
    user_id     BIGINT   NOT NULL COMMENT '用户ID',
    role_id     BIGINT   NOT NULL COMMENT '角色ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ============================================================
-- sys_role_permission：角色权限关联表
-- ============================================================
CREATE TABLE sys_role_permission (
    id            BIGINT   NOT NULL COMMENT '主键',
    role_id       BIGINT   NOT NULL COMMENT '角色ID',
    permission_id BIGINT   NOT NULL COMMENT '权限ID',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';
