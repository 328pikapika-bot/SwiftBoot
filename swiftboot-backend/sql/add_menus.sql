-- 新增菜单SQL
-- 岗位管理
INSERT INTO `sys_menu` VALUES (105, 1, '岗位管理', 'C', 'post', 'system/post/index', 'system:post:list', 'briefcase', 6, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '岗位管理菜单');

-- 定时任务
INSERT INTO `sys_menu` VALUES (106, 1, '定时任务', 'C', 'job', 'system/job/index', 'monitor:job:list', 'timer', 7, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '定时任务菜单');

-- 系统公告
INSERT INTO `sys_menu` VALUES (107, 1, '系统公告', 'C', 'notice', 'system/notice/index', 'system:notice:list', 'bell', 8, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '系统公告菜单');

-- 站内消息
INSERT INTO `sys_menu` VALUES (108, 1, '站内消息', 'C', 'message', 'system/message/index', 'system:message:list', 'message', 9, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '站内消息菜单');

-- 文件管理
INSERT INTO `sys_menu` VALUES (109, 1, '文件管理', 'C', 'file', 'system/file/index', 'system:file:list', 'folder', 10, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '文件管理菜单');

-- 配置管理（系统工具子菜单）
INSERT INTO `sys_menu` VALUES (110, 1, '配置管理', 'C', 'config', 'tool/config/index', 'tool:config:list', 'Setting', 4, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), 'AI配置管理菜单');

-- 给管理员角色赋予所有新菜单权限
INSERT INTO `role_menu` SELECT 1, id FROM sys_menu WHERE id IN (105, 106, 107, 108, 109, 110);
