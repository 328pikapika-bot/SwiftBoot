package com.swiftboot.admin.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录请求
 */
@Data
@Schema(description = "登录请求")
public class LoginDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", required = true)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", required = true)
    private String password;

    @Schema(description = "验证码")
    private String code;

    @Schema(description = "验证码UUID")
    private String uuid;
}
