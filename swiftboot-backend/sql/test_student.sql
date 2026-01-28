DROP TABLE IF EXISTS `test_student`;
CREATE TABLE `test_student` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '学生ID',
  `student_name` varchar(30) DEFAULT '' COMMENT '学生名称',
  `age` int(11) DEFAULT NULL COMMENT '年龄',
  `sex` char(1) DEFAULT '0' COMMENT '性别（0男 1女 2未知）',
  `birthday` datetime DEFAULT NULL COMMENT '生日',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='测试学生表';
