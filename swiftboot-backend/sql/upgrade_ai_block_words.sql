CREATE TABLE IF NOT EXISTS `sys_ai_block_category` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `category_name` varchar(40) NOT NULL COMMENT '分类名称',
  `category_code` varchar(40) NOT NULL COMMENT '分类编码',
  `sort` int NOT NULL DEFAULT '100' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态（0启用 1停用）',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_ai_block_category_code` (`category_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 屏蔽词分类表';

CREATE TABLE IF NOT EXISTS `sys_ai_block_word` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `word_text` varchar(80) NOT NULL COMMENT '屏蔽词内容',
  `match_type` varchar(16) NOT NULL DEFAULT 'contains' COMMENT '匹配方式（contains/exact）',
  `sort` int NOT NULL DEFAULT '100' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态（0启用 1停用）',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`id`),
  KEY `idx_sys_ai_block_word_category_id` (`category_id`),
  KEY `idx_sys_ai_block_word_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 屏蔽词表';
