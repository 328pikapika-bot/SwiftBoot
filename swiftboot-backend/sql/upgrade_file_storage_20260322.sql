ALTER TABLE `sys_file`
    ADD COLUMN `storage_bucket` varchar(200) DEFAULT '' COMMENT '存储桶' AFTER `storage_type`,
    ADD COLUMN `mime_type` varchar(100) DEFAULT '' COMMENT 'MIME类型' AFTER `storage_bucket`,
    ADD COLUMN `visibility` varchar(20) DEFAULT 'private' COMMENT '可见性' AFTER `mime_type`,
    ADD COLUMN `biz_type` varchar(100) DEFAULT '' COMMENT '业务类型' AFTER `visibility`,
    ADD COLUMN `biz_id` bigint DEFAULT NULL COMMENT '业务主键' AFTER `biz_type`;
