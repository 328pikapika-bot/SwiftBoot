/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80033 (8.0.33)
 Source Host           : localhost:3306
 Source Schema         : swiftboot

 Target Server Type    : MySQL
 Target Server Version : 80033 (8.0.33)
 File Encoding         : 65001

 Date: 28/01/2026 23:55:51
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for test_project
-- ----------------------------
DROP TABLE IF EXISTS `test_project`;
CREATE TABLE `test_project`  (
  `id` bigint NOT NULL COMMENT '项目ID',
  `project_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目名称',
  `project_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '项目编号',
  `project_type` tinyint NULL DEFAULT 1 COMMENT '项目类型（1内部项目 2外包项目 3合作项目）',
  `manager_id` bigint NULL DEFAULT NULL COMMENT '项目经理ID',
  `manager_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '项目经理姓名',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '所属部门ID',
  `start_date` date NULL DEFAULT NULL COMMENT '开始日期',
  `end_date` date NULL DEFAULT NULL COMMENT '结束日期',
  `budget` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '项目预算',
  `progress` int NULL DEFAULT 0 COMMENT '项目进度（0-100）',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态（0进行中 1已完成 2已暂停 3已取消）',
  `priority` tinyint NULL DEFAULT 2 COMMENT '优先级（1低 2中 3高）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '项目描述',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_project_code`(`project_code` ASC) USING BTREE,
  INDEX `idx_manager_id`(`manager_id` ASC) USING BTREE,
  INDEX `idx_dept_id`(`dept_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '示例_项目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of test_project
-- ----------------------------
INSERT INTO `test_project` VALUES (1, 'SwiftBoot框架开发', 'PRJ-2024-001', 1, 1, 'admin', 100, '2024-01-01', '2024-12-31', 500000.00, 60, 0, 3, 'SwiftBoot轻量级快速开发框架', 0, 'admin', '2026-01-21 23:47:27', 'admin', '2026-01-21 23:47:27', NULL);
INSERT INTO `test_project` VALUES (2, '官网改版项目', 'PRJ-2024-002', 1, 2, 'swiftboot', 100, '2024-03-01', '2024-06-30', 100000.00, 100, 1, 2, '公司官网UI改版升级', 0, 'admin', '2026-01-21 23:47:37', 'admin', '2026-01-21 23:47:37', NULL);
INSERT INTO `test_project` VALUES (3, '电商平台开发', 'PRJ-2024-003', 2, 1, 'admin', 101, '2024-06-01', '2025-06-01', 2000000.00, 30, 0, 3, '客户电商平台定制开发', 0, 'admin', '2026-01-21 23:47:37', 'admin', '2026-01-21 23:47:37', NULL);

SET FOREIGN_KEY_CHECKS = 1;
