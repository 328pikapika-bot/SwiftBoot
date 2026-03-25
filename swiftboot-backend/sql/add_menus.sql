-- SwiftBoot 菜单兼容迁移脚本
-- 用途：
-- 1. 已初始化旧版本数据库时，用于把菜单迁移到新的产品分组结构。
-- 2. 新环境初始化仍优先执行 swiftboot.sql。

USE `swiftboot`;

-- 一、补齐新的一级目录
INSERT INTO `sys_menu`
SELECT 4, 0, '示例业务', 'M', 'examples', NULL, NULL, 'folder', 6, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '示例业务目录'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 4);

INSERT INTO `sys_menu`
SELECT 5, 0, '平台中心', 'M', 'platform', NULL, NULL, 'widgets', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '平台中心目录'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 5);

-- 二、补齐缺失菜单
INSERT INTO `sys_menu`
SELECT 202, 2, 'AI看板', 'C', 'ai-dashboard', 'monitor/ai-session/index', 'monitor:ai-session:list', 'insights', 4, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), 'AI会话分析看板'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 202);

INSERT INTO `sys_menu`
SELECT 203, 2, '基础资源', 'C', 'server', 'monitor/server/index', 'monitor:server:list', 'histogram', 3, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '基础资源监控菜单'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 203);

INSERT INTO `sys_menu`
SELECT 204, 2, '屏蔽词命中日志', 'C', 'ai-block-hit', 'monitor/ai-block-hit/index', 'monitor:ai-block-hit:list', 'shield', 5, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), 'AI屏蔽词命中日志菜单'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 204);

INSERT INTO `sys_menu`
SELECT 105, 1, '岗位管理', 'C', 'post', 'system/post/index', 'system:post:list', 'briefcase', 5, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '岗位管理菜单'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 105);

INSERT INTO `sys_menu`
SELECT 106, 5, '定时任务', 'C', 'job', 'system/job/index', 'monitor:job:list', 'schedule', 5, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '定时任务菜单'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 106);

INSERT INTO `sys_menu`
SELECT 107, 5, '系统公告', 'C', 'notice', 'system/notice/index', 'system:notice:list', 'campaign', 3, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '系统公告菜单'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 107);

INSERT INTO `sys_menu`
SELECT 108, 5, '站内消息', 'C', 'message', 'system/message/index', 'system:message:list', 'mail', 4, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '站内消息菜单'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 108);

INSERT INTO `sys_menu`
SELECT 109, 5, '文件管理', 'C', 'file', 'system-file-manage', 'system:file:list', 'folder', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '文件管理菜单'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 109);

INSERT INTO `sys_menu`
SELECT 110, 5, '配置管理', 'C', 'config', 'tool-config-page', 'tool:config:list', 'tune', 6, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '平台配置管理菜单'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 110);

INSERT INTO `sys_menu`
SELECT 210, 0, '智能助手', 'M', 'assistant', NULL, NULL, 'smart_toy', 5, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '智能助手目录'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 210);

INSERT INTO `sys_menu`
SELECT 211, 210, '会话窗口', 'C', 'chat', 'ai/chat/index', NULL, 'chat', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '智能会话窗口'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 211);

INSERT INTO `sys_menu`
SELECT 212, 210, 'AI配置', 'C', 'config', 'tool/config/index', 'tool:config:list', 'tune', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), 'AI配置菜单'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 212);

INSERT INTO `sys_menu`
SELECT 300, 3, '代码生成', 'C', 'gen', 'tool/gen/index', 'tool:gen:list', 'code', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '代码生成菜单'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 300);

INSERT INTO `sys_menu`
SELECT 301, 3, '图标选择', 'C', 'icon', 'tool/icon/index', 'tool:icon:list', 'star', 3, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '图标选择菜单'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 301);

INSERT INTO `sys_menu`
SELECT 302, 3, '图表设计', 'C', 'chart', 'tool/chart/index', 'tool:chart:list', 'pie_chart', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '图表设计菜单'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 302);

INSERT INTO `sys_menu`
SELECT 400, 4, '项目示例', 'C', 'project', 'testProject/index', 'test:testProject:list', 'folder', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '项目示例菜单'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 400);

-- 三、修正已有菜单到新的产品分组
UPDATE `sys_menu` SET `menu_name` = '权限中心', `path` = 'access', `icon` = 'admin_panel_settings', `sort` = 1, `remark` = '权限中心目录' WHERE `id` = 1;
UPDATE `sys_menu` SET `menu_name` = '平台中心', `path` = 'platform', `icon` = 'widgets', `sort` = 2, `remark` = '平台中心目录' WHERE `id` = 5;
UPDATE `sys_menu` SET `menu_name` = '监控中心', `path` = 'monitor', `icon` = 'monitor_heart', `sort` = 3, `remark` = '监控中心目录' WHERE `id` = 2;
UPDATE `sys_menu` SET `menu_name` = '开发工具', `path` = 'develop', `icon` = 'build', `sort` = 4, `remark` = '开发工具目录' WHERE `id` = 3;
UPDATE `sys_menu` SET `menu_name` = '智能助手', `path` = 'assistant', `icon` = 'smart_toy', `sort` = 5, `remark` = '智能助手目录' WHERE `id` = 210;
UPDATE `sys_menu` SET `menu_name` = '示例业务', `path` = 'examples', `icon` = 'folder', `sort` = 6, `remark` = '示例业务目录' WHERE `id` = 4;

UPDATE `sys_menu` SET `parent_id` = 1, `sort` = 1 WHERE `id` = 100;
UPDATE `sys_menu` SET `parent_id` = 1, `sort` = 2 WHERE `id` = 101;
UPDATE `sys_menu` SET `parent_id` = 1, `sort` = 3 WHERE `id` = 102;
UPDATE `sys_menu` SET `parent_id` = 1, `sort` = 4 WHERE `id` = 103;
UPDATE `sys_menu` SET `parent_id` = 1, `sort` = 5 WHERE `id` = 105;

UPDATE `sys_menu` SET `parent_id` = 5, `path` = 'dict', `component` = 'system/dict/index', `icon` = 'book', `sort` = 1 WHERE `id` = 104;
UPDATE `sys_menu` SET `parent_id` = 5, `path` = 'file', `component` = 'system-file-manage', `icon` = 'folder', `sort` = 2 WHERE `id` = 109;
UPDATE `sys_menu` SET `parent_id` = 5, `path` = 'notice', `component` = 'system/notice/index', `icon` = 'campaign', `sort` = 3 WHERE `id` = 107;
UPDATE `sys_menu` SET `parent_id` = 5, `path` = 'message', `component` = 'system/message/index', `icon` = 'mail', `sort` = 4 WHERE `id` = 108;
UPDATE `sys_menu` SET `parent_id` = 5, `path` = 'job', `component` = 'system/job/index', `icon` = 'schedule', `sort` = 5 WHERE `id` = 106;
UPDATE `sys_menu` SET `parent_id` = 5, `path` = 'config', `component` = 'tool-config-page', `icon` = 'tune', `sort` = 6 WHERE `id` = 110;

UPDATE `sys_menu` SET `parent_id` = 2, `path` = 'operlog', `sort` = 1 WHERE `id` = 200;
UPDATE `sys_menu` SET `parent_id` = 2, `path` = 'loginlog', `sort` = 2 WHERE `id` = 201;
UPDATE `sys_menu` SET `parent_id` = 2, `path` = 'server', `component` = 'monitor/server/index', `sort` = 3 WHERE `id` = 203;
UPDATE `sys_menu` SET `parent_id` = 2, `path` = 'ai-dashboard', `component` = 'monitor/ai-session/index', `sort` = 4 WHERE `id` = 202;

UPDATE `sys_menu` SET `parent_id` = 3, `path` = 'gen', `sort` = 1 WHERE `id` = 300;
UPDATE `sys_menu` SET `parent_id` = 3, `path` = 'chart', `component` = 'tool/chart/index', `icon` = 'pie_chart', `sort` = 2 WHERE `id` = 302;
UPDATE `sys_menu` SET `parent_id` = 3, `path` = 'icon', `component` = 'tool/icon/index', `icon` = 'star', `sort` = 3 WHERE `id` = 301;

UPDATE `sys_menu` SET `parent_id` = 210, `path` = 'chat', `component` = 'ai/chat/index', `icon` = 'chat', `sort` = 1 WHERE `id` = 211;
UPDATE `sys_menu` SET `parent_id` = 210, `path` = 'config', `component` = 'tool/config/index', `perms` = 'tool:config:list', `icon` = 'tune', `sort` = 2 WHERE `id` = 212;

UPDATE `sys_menu` SET `parent_id` = 4, `menu_name` = '项目示例', `path` = 'project', `component` = 'testProject/index', `perms` = 'test:testProject:list', `remark` = '项目示例菜单', `sort` = 1 WHERE `id` = 400;
-- 四、补齐相关按钮权限
INSERT INTO `sys_menu`
SELECT 2021, 202, 'AI会话查询', 'F', '', '', 'monitor:ai-session:query', '', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 2021);

INSERT INTO `sys_menu`
SELECT 2022, 202, 'AI会话删除', 'F', '', '', 'monitor:ai-session:remove', '', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 2022);

INSERT INTO `sys_menu`
SELECT 2023, 202, 'AI会话清空', 'F', '', '', 'monitor:ai-session:clean', '', 3, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 2023);

INSERT INTO `sys_menu`
SELECT 2031, 203, '资源监控查询', 'F', '', '', 'monitor:server:query', '', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 2031);

INSERT INTO `sys_menu`
SELECT 2041, 204, '命中日志查询', 'F', '', '', 'monitor:ai-block-hit:list', '', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 2041);

INSERT INTO `sys_menu`
SELECT 1101, 110, '配置查询', 'F', '', '', 'tool:config:list', '', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 1101);

INSERT INTO `sys_menu`
SELECT 1102, 110, '配置修改', 'F', '', '', 'tool:config:edit', '', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 1102);

INSERT INTO `sys_menu`
SELECT 4001, 400, '项目示例查询', 'F', '', '', 'test:testProject:query', '', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 4001);

INSERT INTO `sys_menu`
SELECT 4002, 400, '项目示例新增', 'F', '', '', 'test:testProject:add', '', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 4002);

INSERT INTO `sys_menu`
SELECT 4003, 400, '项目示例修改', 'F', '', '', 'test:testProject:edit', '', 3, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 4003);

INSERT INTO `sys_menu`
SELECT 4004, 400, '项目示例删除', 'F', '', '', 'test:testProject:remove', '', 4, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 4004);

INSERT INTO `sys_menu`
SELECT 4005, 400, '项目示例导入', 'F', '', '', 'test:testProject:import', '', 5, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 4005);

INSERT INTO `sys_menu`
SELECT 4006, 400, '项目示例导出', 'F', '', '', 'test:testProject:export', '', 6, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 4006);

INSERT INTO `sys_menu`
SELECT 4007, 400, '项目示例模板下载', 'F', '', '', 'test:testProject:template', '', 7, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 4007);

-- 五、管理员授权
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, m.id
FROM `sys_menu` m
WHERE m.id IN (
  4, 5, 105, 106, 107, 108, 109, 110, 1101, 1102,
  202, 2021, 2022, 2023, 203, 2031, 210, 211, 212,
  300, 301, 302,
  400, 4001, 4002, 4003, 4004, 4005, 4006, 4007
)
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_role_menu` rm
    WHERE rm.role_id = 1
      AND rm.menu_id = m.id
  );

-- 清理旧的测试菜单与测试学生残留
DELETE FROM `sys_role_menu`
WHERE `menu_id` IN (401, 4011, 4012, 4013, 4014, 2016505823958016001, 2021512895305023489);

DELETE FROM `sys_menu`
WHERE `id` IN (401, 4011, 4012, 4013, 4014, 2016505823958016001, 2021512895305023489)
   OR `perms` LIKE 'student:testStudent:%';
