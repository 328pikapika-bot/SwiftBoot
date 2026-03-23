package com.swiftboot.admin.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * User save DTO.
 */
@Data
@Schema(description = "用户写入 DTO")
public class SysUserSaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "部门ID")
    private Long deptId;

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码")
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "性别")
    private Integer gender;

    @Schema(description = "头像")
    private String avatar;

    @NotNull(message = "用户状态不能为空")
    @Schema(description = "用户状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "角色ID列表")
    private List<Long> roleIds;
}
