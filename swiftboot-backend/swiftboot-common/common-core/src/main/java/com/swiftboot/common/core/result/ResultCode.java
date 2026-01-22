package com.swiftboot.common.core.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有相关权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),

    // 服务端错误 5xx
    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    // 业务错误 1xxx
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "用户名或密码错误"),
    USER_DISABLED(1003, "用户已被禁用"),
    USER_EXISTS(1004, "用户已存在"),
    
    ROLE_NOT_FOUND(1101, "角色不存在"),
    ROLE_EXISTS(1102, "角色已存在"),
    ROLE_IN_USE(1103, "角色正在使用中，无法删除"),

    MENU_NOT_FOUND(1201, "菜单不存在"),
    MENU_HAS_CHILDREN(1202, "存在子菜单，无法删除"),

    DEPT_NOT_FOUND(1301, "部门不存在"),
    DEPT_HAS_CHILDREN(1302, "存在子部门，无法删除"),
    DEPT_HAS_USER(1303, "部门下存在用户，无法删除"),

    DICT_NOT_FOUND(1401, "字典不存在"),
    DICT_TYPE_EXISTS(1402, "字典类型已存在"),

    FILE_UPLOAD_ERROR(1501, "文件上传失败"),
    FILE_NOT_FOUND(1502, "文件不存在"),
    FILE_TYPE_ERROR(1503, "文件类型不允许");

    private final int code;
    private final String msg;
}
