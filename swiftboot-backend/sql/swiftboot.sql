-- ----------------------------
-- SwiftBoot 数据库初始化脚本
-- ----------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 创建数据库
-- ----------------------------
CREATE DATABASE IF NOT EXISTS `swiftboot` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `swiftboot`;

-- ----------------------------
-- 部门表
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
                            `id` bigint NOT NULL COMMENT '部门ID',
                            `parent_id` bigint DEFAULT 0 COMMENT '父部门ID',
                            `ancestors` varchar(500) DEFAULT '' COMMENT '祖级列表',
                            `dept_name` varchar(50) DEFAULT '' COMMENT '部门名称',
                            `sort` int DEFAULT 0 COMMENT '排序',
                            `leader` varchar(20) DEFAULT NULL COMMENT '负责人',
                            `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
                            `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
                            `default_role_id` bigint DEFAULT 2 COMMENT '默认角色ID（新增用户时的默认角色）',
                            `status` tinyint DEFAULT 0 COMMENT '状态（0正常 1禁用）',
                            `deleted` tinyint DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
                            `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                            `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                            `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                            `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                            `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- ----------------------------
-- 用户表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
                            `id` bigint NOT NULL COMMENT '用户ID',
                            `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
                            `username` varchar(50) NOT NULL COMMENT '用户名',
                            `password` varchar(100) DEFAULT '' COMMENT '密码',
                            `nickname` varchar(50) DEFAULT '' COMMENT '昵称',
                            `email` varchar(50) DEFAULT '' COMMENT '邮箱',
                            `phone` varchar(20) DEFAULT '' COMMENT '手机号',
                            `gender` tinyint DEFAULT 0 COMMENT '性别（0男 1女 2未知）',
                            `avatar` varchar(200) DEFAULT '' COMMENT '头像',
                            `status` tinyint DEFAULT 0 COMMENT '状态（0正常 1禁用）',
                            `login_ip` varchar(50) DEFAULT '' COMMENT '最后登录IP',
                            `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
                            `deleted` tinyint DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
                            `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                            `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                            `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                            `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                            `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ----------------------------
-- 角色表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
                            `id` bigint NOT NULL COMMENT '角色ID',
                            `role_name` varchar(50) NOT NULL COMMENT '角色名称',
                            `role_key` varchar(100) NOT NULL COMMENT '角色标识',
                            `sort` int DEFAULT 0 COMMENT '排序',
                            `status` tinyint DEFAULT 0 COMMENT '状态（0正常 1禁用）',
                            `deleted` tinyint DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
                            `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                            `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                            `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                            `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                            `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_role_key` (`role_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ----------------------------
-- 菜单表
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
                            `id` bigint NOT NULL COMMENT '菜单ID',
                            `parent_id` bigint DEFAULT 0 COMMENT '父菜单ID',
                            `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
                            `menu_type` char(1) DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
                            `path` varchar(200) DEFAULT '' COMMENT '路由地址',
                            `component` varchar(200) DEFAULT NULL COMMENT '组件路径',
                            `perms` varchar(100) DEFAULT NULL COMMENT '权限标识',
                            `icon` varchar(100) DEFAULT '#' COMMENT '菜单图标',
                            `sort` int DEFAULT 0 COMMENT '排序',
                            `visible` tinyint DEFAULT 0 COMMENT '是否可见（0显示 1隐藏）',
                            `status` tinyint DEFAULT 0 COMMENT '状态（0正常 1禁用）',
                            `deleted` tinyint DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
                            `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                            `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                            `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                            `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                            `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

-- ----------------------------
-- 用户-角色关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
                                 `user_id` bigint NOT NULL COMMENT '用户ID',
                                 `role_id` bigint NOT NULL COMMENT '角色ID',
                                 PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-角色关联表';

-- ----------------------------
-- 角色-菜单关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
                                 `role_id` bigint NOT NULL COMMENT '角色ID',
                                 `menu_id` bigint NOT NULL COMMENT '菜单ID',
                                 PRIMARY KEY (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色-菜单关联表';

-- ----------------------------
-- 字典类型表
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict` (
                            `id` bigint NOT NULL COMMENT '字典ID',
                            `dict_name` varchar(100) DEFAULT '' COMMENT '字典名称',
                            `dict_type` varchar(100) DEFAULT '' COMMENT '字典类型',
                            `status` tinyint DEFAULT 0 COMMENT '状态（0正常 1禁用）',
                            `deleted` tinyint DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
                            `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                            `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                            `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                            `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                            `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典类型表';

-- ----------------------------
-- 字典数据表
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
                                 `id` bigint NOT NULL COMMENT '字典数据ID',
                                 `dict_type` varchar(100) DEFAULT '' COMMENT '字典类型',
                                 `dict_label` varchar(100) DEFAULT '' COMMENT '字典标签',
                                 `dict_value` varchar(100) DEFAULT '' COMMENT '字典值',
                                 `sort` int DEFAULT 0 COMMENT '排序',
                                 `css_class` varchar(100) DEFAULT NULL COMMENT '样式属性',
                                 `list_class` varchar(100) DEFAULT NULL COMMENT '表格回显样式',
                                 `is_default` tinyint DEFAULT 0 COMMENT '是否默认（0否 1是）',
                                 `status` tinyint DEFAULT 0 COMMENT '状态（0正常 1禁用）',
                                 `deleted` tinyint DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
                                 `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                                 `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                 `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                                 `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                 `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典数据表';

-- ----------------------------
-- 操作日志表
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log` (
                                `id` bigint NOT NULL COMMENT '日志ID',
                                `title` varchar(50) DEFAULT '' COMMENT '模块标题',
                                `business_type` int DEFAULT 0 COMMENT '业务类型',
                                `method` varchar(200) DEFAULT '' COMMENT '方法名称',
                                `request_method` varchar(10) DEFAULT '' COMMENT '请求方式',
                                `oper_name` varchar(50) DEFAULT '' COMMENT '操作人员',
                                `oper_url` varchar(500) DEFAULT '' COMMENT '请求URL',
                                `oper_ip` varchar(50) DEFAULT '' COMMENT '主机地址',
                                `oper_param` text COMMENT '请求参数',
                                `json_result` text COMMENT '返回参数',
                                `status` int DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
                                `error_msg` text COMMENT '错误消息',
                                `oper_time` datetime DEFAULT NULL COMMENT '操作时间',
                                `cost_time` bigint DEFAULT 0 COMMENT '消耗时间',
                                PRIMARY KEY (`id`),
                                KEY `idx_oper_time` (`oper_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ----------------------------
-- 登录日志表
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log` (
                                 `id` bigint NOT NULL COMMENT '日志ID',
                                 `username` varchar(50) DEFAULT '' COMMENT '用户名',
                                 `login_ip` varchar(50) DEFAULT '' COMMENT '登录IP',
                                 `login_location` varchar(255) DEFAULT '' COMMENT '登录地点',
                                 `browser` varchar(50) DEFAULT '' COMMENT '浏览器类型',
                                 `os` varchar(50) DEFAULT '' COMMENT '操作系统',
                                 `status` tinyint DEFAULT 0 COMMENT '登录状态（0成功 1失败）',
                                 `msg` varchar(255) DEFAULT '' COMMENT '提示消息',
                                 `login_time` datetime DEFAULT NULL COMMENT '登录时间',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- ----------------------------
-- 文件表
-- ----------------------------
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file` (
                            `id` bigint NOT NULL COMMENT '文件ID',
                            `file_name` varchar(200) DEFAULT '' COMMENT '文件名称',
                            `original_name` varchar(200) DEFAULT '' COMMENT '原始名称',
                            `file_suffix` varchar(20) DEFAULT '' COMMENT '文件后缀',
                            `file_path` varchar(500) DEFAULT '' COMMENT '文件路径',
                            `file_size` bigint DEFAULT 0 COMMENT '文件大小',
                            `storage_type` varchar(20) DEFAULT '' COMMENT '存储类型',
                            `storage_bucket` varchar(200) DEFAULT '' COMMENT '存储桶',
                            `mime_type` varchar(100) DEFAULT '' COMMENT 'MIME类型',
                            `visibility` varchar(20) DEFAULT 'private' COMMENT '可见性',
                            `biz_type` varchar(100) DEFAULT '' COMMENT '业务类型',
                            `biz_id` bigint DEFAULT NULL COMMENT '业务主键',
                            `url` varchar(500) DEFAULT '' COMMENT '访问地址',
                            `deleted` tinyint DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
                            `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                            `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                            `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                            `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                            `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件表';

-- ----------------------------
-- 代码生成表
-- ----------------------------
DROP TABLE IF EXISTS `gen_table`;
CREATE TABLE `gen_table` (
                             `id` bigint NOT NULL COMMENT '表ID',
                             `table_name` varchar(200) DEFAULT '' COMMENT '表名称',
                             `table_comment` varchar(500) DEFAULT '' COMMENT '表描述',
                             `class_name` varchar(100) DEFAULT '' COMMENT '实体类名称',
                             `package_name` varchar(100) DEFAULT '' COMMENT '生成包路径',
                             `module_name` varchar(50) DEFAULT '' COMMENT '生成模块名',
                             `business_name` varchar(50) DEFAULT '' COMMENT '生成业务名',
                             `function_name` varchar(100) DEFAULT '' COMMENT '生成功能名',
                             `author` varchar(50) DEFAULT '' COMMENT '生成作者',
                             `gen_path` varchar(200) DEFAULT '' COMMENT '生成路径',
                             `gen_type` char(1) DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
                             `options` varchar(1000) DEFAULT NULL COMMENT '其他生成选项',
                             `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                             `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                             `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代码生成表';

-- ----------------------------
-- 代码生成字段表
-- ----------------------------
DROP TABLE IF EXISTS `gen_table_column`;
CREATE TABLE `gen_table_column` (
                                    `id` bigint NOT NULL COMMENT '字段ID',
                                    `table_id` bigint DEFAULT NULL COMMENT '表ID',
                                    `column_name` varchar(200) DEFAULT '' COMMENT '字段名称',
                                    `column_comment` varchar(500) DEFAULT '' COMMENT '字段描述',
                                    `column_type` varchar(100) DEFAULT '' COMMENT '字段类型',
                                    `java_type` varchar(50) DEFAULT '' COMMENT 'Java类型',
                                    `java_field` varchar(200) DEFAULT '' COMMENT 'Java字段名',
                                    `is_pk` char(1) DEFAULT '0' COMMENT '是否主键（1是）',
                                    `is_increment` char(1) DEFAULT '0' COMMENT '是否自增（1是）',
                                    `is_required` char(1) DEFAULT '0' COMMENT '是否必填（1是）',
                                    `is_insert` char(1) DEFAULT '0' COMMENT '是否为插入字段（1是）',
                                    `is_edit` char(1) DEFAULT '0' COMMENT '是否为编辑字段（1是）',
                                    `is_list` char(1) DEFAULT '0' COMMENT '是否为列表字段（1是）',
                                    `is_query` char(1) DEFAULT '0' COMMENT '是否为查询字段（1是）',
                                    `query_type` varchar(200) DEFAULT 'EQ' COMMENT '查询方式',
                                    `html_type` varchar(200) DEFAULT '' COMMENT '显示类型',
                                    `dict_type` varchar(200) DEFAULT '' COMMENT '字典类型',
                                    `sort` int DEFAULT 0 COMMENT '排序',
                                    PRIMARY KEY (`id`),
                                    KEY `idx_table_id` (`table_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代码生成字段表';

-- ----------------------------
-- 初始化数据
-- ----------------------------

-- 部门数据
INSERT INTO `sys_dept` VALUES (1, 0, '0', 'SwiftBoot科技', 0, 'admin', '18888888888', 'admin@swiftboot.com', 2, 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);
INSERT INTO `sys_dept` VALUES (100, 1, '0,1', '技术部', 1, NULL, NULL, NULL, 2, 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);
INSERT INTO `sys_dept` VALUES (101, 1, '0,1', '运营部', 2, NULL, NULL, NULL, 2, 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);

-- 用户数据（密码: 123456）
-- 密码: 123456 (BCrypt加密)
INSERT INTO `sys_user` VALUES (1, 100, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '超级管理员', 'admin@swiftboot.com', '18888888888', 0, '', 0, '', NULL, 0, 'admin', NOW(), 'admin', NOW(), '系统管理员');
INSERT INTO `sys_user` VALUES (2, 100, 'swiftboot', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '普通用户', 'user@swiftboot.com', '18666666666', 0, '', 0, '', NULL, 0, 'admin', NOW(), 'admin', NOW(), '测试用户');

-- 角色数据
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'admin', 1, 0, 0, 'admin', NOW(), 'admin', NOW(), '超级管理员');
INSERT INTO `sys_role` VALUES (2, '普通角色', 'common', 2, 0, 0, 'admin', NOW(), 'admin', NOW(), '普通角色');

-- 菜单数据
-- 说明：
-- 1. 本文件中的菜单初始化数据为 SwiftBoot 的权威基线。
-- 2. 新环境初始化请以本文件为准，避免依赖额外的补丁脚本拼接基础菜单。
-- 3. add_menus.sql 仅保留给历史环境做兼容补丁使用。
-- 权限中心
INSERT INTO `sys_menu` VALUES (1, 0, '权限中心', 'M', 'access', NULL, NULL, 'admin_panel_settings', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '权限中心目录');
INSERT INTO `sys_menu` VALUES (100, 1, '用户管理', 'C', 'user', 'system/user/index', 'system:user:list', 'user', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '用户管理菜单');
INSERT INTO `sys_menu` VALUES (101, 1, '角色管理', 'C', 'role', 'system/role/index', 'system:role:list', 'manage_accounts', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '角色管理菜单');
INSERT INTO `sys_menu` VALUES (102, 1, '菜单管理', 'C', 'menu', 'system/menu/index', 'system:menu:list', 'menu', 3, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '菜单管理菜单');
INSERT INTO `sys_menu` VALUES (103, 1, '部门管理', 'C', 'dept', 'system/dept/index', 'system:dept:list', 'corporate_fare', 4, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '部门管理菜单');
INSERT INTO `sys_menu` VALUES (105, 1, '岗位管理', 'C', 'post', 'system/post/index', 'system:post:list', 'briefcase', 5, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '岗位管理菜单');

-- 平台中心
INSERT INTO `sys_menu` VALUES (5, 0, '平台中心', 'M', 'platform', NULL, NULL, 'widgets', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '平台中心目录');
INSERT INTO `sys_menu` VALUES (104, 5, '字典管理', 'C', 'dict', 'system/dict/index', 'system:dict:list', 'book', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '字典管理菜单');
INSERT INTO `sys_menu` VALUES (109, 5, '文件管理', 'C', 'file', 'system-file-manage', 'system:file:list', 'folder', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '文件管理菜单');
INSERT INTO `sys_menu` VALUES (107, 5, '系统公告', 'C', 'notice', 'system/notice/index', 'system:notice:list', 'campaign', 3, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '系统公告菜单');
INSERT INTO `sys_menu` VALUES (108, 5, '站内消息', 'C', 'message', 'system/message/index', 'system:message:list', 'mail', 4, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '站内消息菜单');
INSERT INTO `sys_menu` VALUES (106, 5, '定时任务', 'C', 'job', 'system/job/index', 'monitor:job:list', 'schedule', 5, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '定时任务菜单');
INSERT INTO `sys_menu` VALUES (110, 5, '配置管理', 'C', 'config', 'tool-config-page', 'tool:config:list', 'tune', 6, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '平台配置管理菜单');

-- 监控中心
INSERT INTO `sys_menu` VALUES (2, 0, '监控中心', 'M', 'monitor', NULL, NULL, 'monitor_heart', 3, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '监控中心目录');
INSERT INTO `sys_menu` VALUES (200, 2, '操作日志', 'C', 'operlog', 'monitor/operlog/index', 'monitor:operlog:list', 'form', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '操作日志菜单');
INSERT INTO `sys_menu` VALUES (201, 2, '登录日志', 'C', 'loginlog', 'monitor/loginlog/index', 'monitor:loginlog:list', 'logininfor', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '登录日志菜单');
INSERT INTO `sys_menu` VALUES (203, 2, '基础资源', 'C', 'server', 'monitor/server/index', 'monitor:server:list', 'histogram', 3, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '基础资源监控菜单');
INSERT INTO `sys_menu` VALUES (202, 2, 'AI看板', 'C', 'ai-dashboard', 'monitor/ai-session/index', 'monitor:ai-session:list', 'insights', 4, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), 'AI会话分析看板');

-- 开发工具
INSERT INTO `sys_menu` VALUES (3, 0, '开发工具', 'M', 'develop', NULL, NULL, 'build', 4, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '开发工具目录');
INSERT INTO `sys_menu` VALUES (300, 3, '代码生成', 'C', 'gen', 'tool/gen/index', 'tool:gen:list', 'code', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '代码生成菜单');
INSERT INTO `sys_menu` VALUES (302, 3, '图表设计', 'C', 'chart', 'tool/chart/index', 'tool:chart:list', 'pie_chart', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '图表设计菜单');
INSERT INTO `sys_menu` VALUES (301, 3, '图标选择', 'C', 'icon', 'tool/icon/index', 'tool:icon:list', 'star', 3, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '图标选择菜单');

-- 智能助手
INSERT INTO `sys_menu` VALUES (210, 0, '智能助手', 'M', 'assistant', NULL, NULL, 'smart_toy', 5, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '智能助手目录');
INSERT INTO `sys_menu` VALUES (211, 210, '会话窗口', 'C', 'chat', 'ai/chat/index', NULL, 'chat', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '智能会话窗口');
INSERT INTO `sys_menu` VALUES (212, 210, 'AI配置', 'C', 'config', 'tool/config/index', 'tool:config:list', 'tune', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), 'AI配置菜单');

-- 示例业务
INSERT INTO `sys_menu` VALUES (4, 0, '示例业务', 'M', 'examples', NULL, NULL, 'folder', 6, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '示例业务目录');
INSERT INTO `sys_menu` VALUES (400, 4, '项目示例', 'C', 'project', 'testProject/index', 'test:testProject:list', 'folder', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '项目示例菜单');

-- 用户管理按钮
INSERT INTO `sys_menu` VALUES (1001, 100, '用户查询', 'F', '', '', 'system:user:query', '', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1002, 100, '用户新增', 'F', '', '', 'system:user:add', '', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1003, 100, '用户修改', 'F', '', '', 'system:user:edit', '', 3, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1004, 100, '用户删除', 'F', '', '', 'system:user:remove', '', 4, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1005, 100, '重置密码', 'F', '', '', 'system:user:resetPwd', '', 5, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');

-- 角色管理按钮
INSERT INTO `sys_menu` VALUES (1011, 101, '角色查询', 'F', '', '', 'system:role:query', '', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1012, 101, '角色新增', 'F', '', '', 'system:role:add', '', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1013, 101, '角色修改', 'F', '', '', 'system:role:edit', '', 3, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1014, 101, '角色删除', 'F', '', '', 'system:role:remove', '', 4, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');

-- 菜单管理按钮
INSERT INTO `sys_menu` VALUES (1021, 102, '菜单查询', 'F', '', '', 'system:menu:query', '', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1022, 102, '菜单新增', 'F', '', '', 'system:menu:add', '', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1023, 102, '菜单修改', 'F', '', '', 'system:menu:edit', '', 3, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1024, 102, '菜单删除', 'F', '', '', 'system:menu:remove', '', 4, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');

-- 部门管理按钮
INSERT INTO `sys_menu` VALUES (1031, 103, '部门查询', 'F', '', '', 'system:dept:query', '', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1032, 103, '部门新增', 'F', '', '', 'system:dept:add', '', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1033, 103, '部门修改', 'F', '', '', 'system:dept:edit', '', 3, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1034, 103, '部门删除', 'F', '', '', 'system:dept:remove', '', 4, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');

-- 字典管理按钮
INSERT INTO `sys_menu` VALUES (1041, 104, '字典查询', 'F', '', '', 'system:dict:query', '', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1042, 104, '字典新增', 'F', '', '', 'system:dict:add', '', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1043, 104, '字典修改', 'F', '', '', 'system:dict:edit', '', 3, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1044, 104, '字典删除', 'F', '', '', 'system:dict:remove', '', 4, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');

-- 岗位管理按钮
INSERT INTO `sys_menu` VALUES (1051, 105, '岗位查询', 'F', '', '', 'system:post:query', '', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1052, 105, '岗位新增', 'F', '', '', 'system:post:add', '', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1053, 105, '岗位修改', 'F', '', '', 'system:post:edit', '', 3, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1054, 105, '岗位删除', 'F', '', '', 'system:post:remove', '', 4, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');

-- 定时任务按钮
INSERT INTO `sys_menu` VALUES (1061, 106, '任务查询', 'F', '', '', 'monitor:job:query', '', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1062, 106, '任务新增', 'F', '', '', 'monitor:job:add', '', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1063, 106, '任务修改', 'F', '', '', 'monitor:job:edit', '', 3, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1064, 106, '任务删除', 'F', '', '', 'monitor:job:remove', '', 4, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1065, 106, '任务状态修改', 'F', '', '', 'monitor:job:changeStatus', '', 5, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1066, 106, '任务立即执行', 'F', '', '', 'monitor:job:run', '', 6, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');

-- 公告管理按钮
INSERT INTO `sys_menu` VALUES (1071, 107, '公告查询', 'F', '', '', 'system:notice:query', '', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1072, 107, '公告新增', 'F', '', '', 'system:notice:add', '', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1073, 107, '公告修改', 'F', '', '', 'system:notice:edit', '', 3, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1074, 107, '公告删除', 'F', '', '', 'system:notice:remove', '', 4, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');

-- 站内消息按钮
INSERT INTO `sys_menu` VALUES (1081, 108, '消息查询', 'F', '', '', 'system:message:query', '', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1082, 108, '消息新增', 'F', '', '', 'system:message:add', '', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1083, 108, '消息修改', 'F', '', '', 'system:message:edit', '', 3, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1084, 108, '消息删除', 'F', '', '', 'system:message:remove', '', 4, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');

-- 文件管理按钮
INSERT INTO `sys_menu` VALUES (1091, 109, '文件查询', 'F', '', '', 'system:file:query', '', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1092, 109, '文件上传', 'F', '', '', 'system:file:upload', '', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1093, 109, '文件删除', 'F', '', '', 'system:file:remove', '', 3, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1094, 109, '文件重命名', 'F', '', '', 'system:file:rename', '', 4, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1095, 109, '文件预览', 'F', '', '', 'system:file:preview', '', 5, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1096, 109, '文件下载', 'F', '', '', 'system:file:download', '', 6, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');

-- 操作日志按钮
INSERT INTO `sys_menu` VALUES (2001, 200, '操作日志查询', 'F', '', '', 'monitor:operlog:query', '', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (2002, 200, '操作日志删除', 'F', '', '', 'monitor:operlog:remove', '', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');

-- 登录日志按钮
INSERT INTO `sys_menu` VALUES (2011, 201, '登录日志查询', 'F', '', '', 'monitor:loginlog:query', '', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (2012, 201, '登录日志删除', 'F', '', '', 'monitor:loginlog:remove', '', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');

-- AI看板与监控按钮
INSERT INTO `sys_menu` VALUES (2021, 202, 'AI会话查询', 'F', '', '', 'monitor:ai-session:query', '', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (2022, 202, 'AI会话删除', 'F', '', '', 'monitor:ai-session:remove', '', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (2023, 202, 'AI会话清空', 'F', '', '', 'monitor:ai-session:clean', '', 3, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (2031, 203, '资源监控查询', 'F', '', '', 'monitor:server:query', '', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');

-- 代码生成按钮
INSERT INTO `sys_menu` VALUES (3001, 300, '生成查询', 'F', '', '', 'tool:gen:query', '', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (3002, 300, '生成修改', 'F', '', '', 'tool:gen:edit', '', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (3003, 300, '生成删除', 'F', '', '', 'tool:gen:remove', '', 3, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (3004, 300, '导入代码', 'F', '', '', 'tool:gen:import', '', 4, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (3005, 300, '预览代码', 'F', '', '', 'tool:gen:preview', '', 5, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (3006, 300, '生成代码', 'F', '', '', 'tool:gen:code', '', 6, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');

-- 图标参考按钮
INSERT INTO `sys_menu` VALUES (3011, 301, '图标查询', 'F', '', '', 'tool:icon:query', '', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');

-- 配置管理按钮
INSERT INTO `sys_menu` VALUES (1101, 110, '配置查询', 'F', '', '', 'tool:config:list', '', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1102, 110, '配置修改', 'F', '', '', 'tool:config:edit', '', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');

-- 示例业务按钮
INSERT INTO `sys_menu` VALUES (4001, 400, '项目示例查询', 'F', '', '', 'test:testProject:query', '', 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (4002, 400, '项目示例新增', 'F', '', '', 'test:testProject:add', '', 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (4003, 400, '项目示例修改', 'F', '', '', 'test:testProject:edit', '', 3, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (4004, 400, '项目示例删除', 'F', '', '', 'test:testProject:remove', '', 4, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (4005, 400, '项目示例导入', 'F', '', '', 'test:testProject:import', '', 5, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (4006, 400, '项目示例导出', 'F', '', '', 'test:testProject:export', '', 6, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (4007, 400, '项目示例模板下载', 'F', '', '', 'test:testProject:template', '', 7, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');

-- 用户-角色关联
INSERT INTO `sys_user_role` VALUES (1, 1);
INSERT INTO `sys_user_role` VALUES (2, 2);

-- 角色-菜单关联（管理员拥有所有权限）
INSERT INTO `sys_role_menu` SELECT 1, id FROM `sys_menu`;

-- 字典类型
INSERT INTO `sys_dict` VALUES (1, '用户性别', 'sys_user_gender', 0, 0, 'admin', NOW(), 'admin', NOW(), '用户性别列表');
INSERT INTO `sys_dict` VALUES (2, '系统状态', 'sys_normal_disable', 0, 0, 'admin', NOW(), 'admin', NOW(), '系统状态列表');
INSERT INTO `sys_dict` VALUES (3, '菜单类型', 'sys_menu_type', 0, 0, 'admin', NOW(), 'admin', NOW(), '菜单类型列表');
INSERT INTO `sys_dict` VALUES (4, '系统是否', 'sys_yes_no', 0, 0, 'admin', NOW(), 'admin', NOW(), '系统是否列表');
INSERT INTO `sys_dict` VALUES (5, '操作类型', 'sys_oper_type', 0, 0, 'admin', NOW(), 'admin', NOW(), '操作类型列表');
INSERT INTO `sys_dict` VALUES (6, '登录状态', 'sys_login_status', 0, 0, 'admin', NOW(), 'admin', NOW(), '登录状态列表');

-- 字典数据
INSERT INTO `sys_dict_data` VALUES (1, 'sys_user_gender', '男', '0', 1, '', 'primary', 1, 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);
INSERT INTO `sys_dict_data` VALUES (2, 'sys_user_gender', '女', '1', 2, '', 'danger', 0, 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);
INSERT INTO `sys_dict_data` VALUES (3, 'sys_user_gender', '未知', '2', 3, '', 'info', 0, 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);
INSERT INTO `sys_dict_data` VALUES (4, 'sys_normal_disable', '正常', '0', 1, '', 'success', 1, 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);
INSERT INTO `sys_dict_data` VALUES (5, 'sys_normal_disable', '禁用', '1', 2, '', 'danger', 0, 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);
INSERT INTO `sys_dict_data` VALUES (6, 'sys_menu_type', '目录', 'M', 1, '', 'primary', 0, 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);
INSERT INTO `sys_dict_data` VALUES (7, 'sys_menu_type', '菜单', 'C', 2, '', 'success', 0, 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);
INSERT INTO `sys_dict_data` VALUES (8, 'sys_menu_type', '按钮', 'F', 3, '', 'warning', 0, 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);
INSERT INTO `sys_dict_data` VALUES (9, 'sys_yes_no', '是', '1', 1, '', 'success', 1, 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);
INSERT INTO `sys_dict_data` VALUES (10, 'sys_yes_no', '否', '0', 2, '', 'danger', 0, 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);
INSERT INTO `sys_dict_data` VALUES (11, 'sys_oper_type', '其他', '0', 1, '', 'info', 0, 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);
INSERT INTO `sys_dict_data` VALUES (12, 'sys_oper_type', '新增', '1', 2, '', 'success', 0, 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);
INSERT INTO `sys_dict_data` VALUES (13, 'sys_oper_type', '修改', '2', 3, '', 'primary', 0, 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);
INSERT INTO `sys_dict_data` VALUES (14, 'sys_oper_type', '删除', '3', 4, '', 'danger', 0, 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);
INSERT INTO `sys_dict_data` VALUES (15, 'sys_login_status', '成功', '0', 1, '', 'success', 0, 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);
INSERT INTO `sys_dict_data` VALUES (16, 'sys_login_status', '失败', '1', 2, '', 'danger', 0, 0, 0, 'admin', NOW(), 'admin', NOW(), NULL);
SET FOREIGN_KEY_CHECKS = 1;
