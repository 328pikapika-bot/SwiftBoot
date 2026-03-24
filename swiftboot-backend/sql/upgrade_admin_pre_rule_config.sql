CREATE TABLE IF NOT EXISTS `sys_ai_admin_pre_rule_config` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `config_key` varchar(64) NOT NULL COMMENT '配置标识，默认 default',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `interception_message` varchar(255) DEFAULT NULL COMMENT '统一拦截文案',
  `rules_json` mediumtext COMMENT '规则列表 JSON',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_ai_admin_pre_rule_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 管理员安全前置规则配置表';
