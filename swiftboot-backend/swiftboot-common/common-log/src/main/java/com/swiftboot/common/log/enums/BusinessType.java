package com.swiftboot.common.log.enums;

/**
 * 业务操作类型
 */
public enum BusinessType {

    /**
     * 其他
     */
    OTHER,

    /**
     * 新增
     */
    INSERT,

    /**
     * 修改
     */
    UPDATE,

    /**
     * 删除
     */
    DELETE,

    /**
     * 查询
     */
    SELECT,

    /**
     * 导出
     */
    EXPORT,

    /**
     * 导入
     */
    IMPORT,

    /**
     * 强退
     */
    FORCE_LOGOUT,

    /**
     * 生成代码
     */
    GENERATE,

    /**
     * 清空数据
     */
    CLEAN
}
