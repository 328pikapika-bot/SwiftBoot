package com.swiftboot.admin.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "访问令牌")
    private String token;
}
