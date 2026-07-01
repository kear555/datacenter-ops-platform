-- ============================================================
-- 数据中心智能运维支撑平台 - 第一阶段 RBAC 表结构
-- ============================================================
-- 说明：
-- 1. 当前 SQL 只使用模拟数据，不包含任何真实数据中心信息。
-- 2. 第一阶段先实现登录鉴权和权限校验，因此只建 sys_user / sys_role / sys_permission 及关联表。
-- 3. 后续资源台账、脚本、任务、执行记录会继续追加业务表。
-- 4. 所有核心表都保留 create_time、update_time、deleted 等工程化通用字段，对应 6.5D 数据库工程规范。

-- 先删除关联表，再删除主表，避免外键或逻辑依赖导致删除顺序问题。
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;

-- ============================================================
-- sys_user：系统用户表
-- ============================================================
-- 用途：保存能登录后台的用户账号。
-- 设计说明：
-- - username：登录名，必须唯一。
-- - password：BCrypt 密文，不保存明文密码。
-- - real_name：真实姓名或展示名，用于操作日志展示“谁做了操作”。
-- - email / phone：你提出需要补充的联系方式字段，后续可用于用户管理、通知或审计追踪。
-- - status：账号状态，1 表示启用，0 表示禁用。
-- - deleted：逻辑删除标记，0 表示正常，1 表示已删除。
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(120) NOT NULL,
    real_name VARCHAR(64) NOT NULL,
    email VARCHAR(128),
    phone VARCHAR(32),
    status TINYINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_sys_user_username UNIQUE (username),
    CONSTRAINT uk_sys_user_email UNIQUE (email),
    CONSTRAINT uk_sys_user_phone UNIQUE (phone)
);

-- ============================================================
-- sys_role：角色表
-- ============================================================
-- 用途：定义一组权限集合，例如管理员、运维人员、只读用户。
-- 设计说明：
-- - role_code：稳定角色编码，代码和初始化数据都使用它识别角色。
-- - role_name：角色展示名，给后台页面或接口文档查看。
-- - status / deleted：支持禁用角色和逻辑删除角色。
CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY,
    role_code VARCHAR(64) NOT NULL,
    role_name VARCHAR(64) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_sys_role_code UNIQUE (role_code)
);

-- ============================================================
-- sys_permission：权限表
-- ============================================================
-- 用途：定义接口级权限点，例如 server:view、task:execute。
-- 设计说明：
-- - permission_code：权限编码，后续 @PreAuthorize("hasAuthority('task:execute')") 会直接使用。
-- - permission_name：权限展示名。
-- - module_name：所属模块，方便后台按资源、脚本、任务等模块管理权限。
CREATE TABLE sys_permission (
    id BIGINT PRIMARY KEY,
    permission_code VARCHAR(128) NOT NULL,
    permission_name VARCHAR(128) NOT NULL,
    module_name VARCHAR(64) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_sys_permission_code UNIQUE (permission_code)
);

-- ============================================================
-- sys_user_role：用户角色关联表
-- ============================================================
-- 用途：表达“一个用户拥有哪些角色”。
-- 设计说明：
-- - 一个用户可以有多个角色。
-- - uk_sys_user_role 防止同一个用户重复绑定同一个角色。
CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sys_user_role UNIQUE (user_id, role_id)
);

-- ============================================================
-- sys_role_permission：角色权限关联表
-- ============================================================
-- 用途：表达“一个角色拥有哪些权限”。
-- 设计说明：
-- - 一个角色可以拥有多个权限。
-- - uk_sys_role_permission 防止同一个角色重复绑定同一个权限。
CREATE TABLE sys_role_permission (
    id BIGINT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sys_role_permission UNIQUE (role_id, permission_id)
);