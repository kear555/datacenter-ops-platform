-- ============================================================
-- 数据中心智能运维支撑平台 - 第一阶段模拟初始化数据
-- ============================================================
-- 说明：
-- 1. 所有账号、邮箱、手机号都是公开模拟数据，不是真实用户信息。
-- 2. 两个账号都使用同一个演示密码：123456。
-- 3. 密码以 BCrypt 密文形式保存，对应 6.5A 中“不能明文存储密码”的要求。
-- 4. admin 用于验收“有权限可以执行任务”；viewer 用于验收“已登录但无权限返回 403”。

-- 初始化两个用户：管理员 admin 和只读用户 viewer。
INSERT INTO sys_user (id, username, password, real_name, email, phone, status, deleted)
VALUES
    (1, 'admin', '{bcrypt}$2b$10$FqyPcJBBckYF5xuAA4NVRumVaAdoLRBAZx4QTuCUycz0poalNr/2e', '演示管理员', 'admin@example.com', '13800000001', 1, 0),
    (2, 'viewer', '{bcrypt}$2b$10$FqyPcJBBckYF5xuAA4NVRumVaAdoLRBAZx4QTuCUycz0poalNr/2e', '只读观察员', 'viewer@example.com', '13800000002', 1, 0);

-- 初始化两个角色：管理员拥有全部权限，只读观察者只能查看。
INSERT INTO sys_role (id, role_code, role_name, status, deleted)
VALUES
    (1, 'ADMIN', '管理员', 1, 0),
    (2, 'VIEWER', '只读观察者', 1, 0);

-- 初始化接口级权限点。
-- 命名规则：模块:动作，例如 task:execute 表示任务执行权限。
INSERT INTO sys_permission (id, permission_code, permission_name, module_name, deleted)
VALUES
    (1, 'server:view', '查看服务器资源', 'resource', 0),
    (2, 'server:create', '新增服务器资源', 'resource', 0),
    (3, 'server:update', '修改服务器资源', 'resource', 0),
    (4, 'script:view', '查看脚本', 'script', 0),
    (5, 'script:create', '登记脚本', 'script', 0),
    (6, 'task:view', '查看巡检任务', 'task', 0),
    (7, 'task:create', '创建巡检任务', 'task', 0),
    (8, 'task:execute', '执行巡检任务', 'task', 0);

-- 绑定用户和角色。
INSERT INTO sys_user_role (id, user_id, role_id)
VALUES
    (1, 1, 1),
    (2, 2, 2);

-- 给管理员绑定全部权限。
INSERT INTO sys_role_permission (id, role_id, permission_id)
VALUES
    (1, 1, 1), (2, 1, 2), (3, 1, 3), (4, 1, 4),
    (5, 1, 5), (6, 1, 6), (7, 1, 7), (8, 1, 8),
    -- 给只读用户只绑定查看类权限，不绑定 task:execute，用于验证 403。
    (9, 2, 1), (10, 2, 4), (11, 2, 6);