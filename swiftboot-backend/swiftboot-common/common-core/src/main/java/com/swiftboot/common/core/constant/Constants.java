package com.swiftboot.common.core.constant;

/**
 * 通用常量
 */
public interface Constants {

    /**
     * 成功标记
     */
    int SUCCESS = 200;

    /**
     * 失败标记
     */
    int FAIL = 500;

    /**
     * 登录用户 Token 前缀
     */
    String LOGIN_USER_KEY = "login_user:";

    /**
     * 验证码前缀
     */
    String CAPTCHA_KEY = "captcha:";

    /**
     * 字典缓存前缀
     */
    String DICT_KEY = "dict:";

    /**
     * 系统配置缓存前缀
     */
    String CONFIG_KEY = "config:";

    /**
     * 用户默认密码
     */
    String DEFAULT_PASSWORD = "123456";

    /**
     * 超级管理员ID
     */
    Long SUPER_ADMIN_ID = 1L;

    /**
     * 顶级部门ID
     */
    Long TOP_DEPT_ID = 0L;

    /**
     * 顶级菜单ID
     */
    Long TOP_MENU_ID = 0L;

    /**
     * 正常状态
     */
    Integer STATUS_NORMAL = 0;

    /**
     * 禁用状态
     */
    Integer STATUS_DISABLE = 1;
}
