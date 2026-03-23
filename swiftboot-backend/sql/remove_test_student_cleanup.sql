USE `swiftboot`;

DELETE FROM `sys_role_menu`
WHERE `menu_id` IN (
    SELECT `id`
    FROM `sys_menu`
    WHERE `id` IN (401, 4011, 4012, 4013, 4014, 2016505823958016001, 2021512895305023489)
       OR `perms` LIKE 'student:testStudent:%'
       OR `menu_name` IN ('测试菜单', '测试学生表', '学生示例')
);

DELETE FROM `sys_menu`
WHERE `id` IN (401, 4011, 4012, 4013, 4014, 2016505823958016001, 2021512895305023489)
   OR `perms` LIKE 'student:testStudent:%'
   OR `menu_name` IN ('测试菜单', '测试学生表', '学生示例');

DELETE FROM `gen_table_column`
WHERE `table_id` IN (
    SELECT `id`
    FROM `gen_table`
    WHERE `table_name` = 'test_student'
);

DELETE FROM `gen_table`
WHERE `table_name` = 'test_student';

DROP TABLE IF EXISTS `test_student`;
