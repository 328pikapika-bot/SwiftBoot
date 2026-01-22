package com.swiftboot.admin.domain.vo;

import com.swiftboot.admin.domain.entity.SysMenu;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 用户信息响应
 */
@Data
@Schema(description = "用户信息响应")
public class UserInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "角色列表")
    private Set<String> roles;

    @Schema(description = "权限列表")
    private Set<String> permissions;

    @Schema(description = "菜单列表")
    private List<SysMenu> menus;
}
