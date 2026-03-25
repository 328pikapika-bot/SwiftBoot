ALTER TABLE `sys_ai_block_category`
  ADD COLUMN IF NOT EXISTS `dict_data_id` bigint DEFAULT NULL COMMENT '关联字典数据ID' AFTER `id`;
