package com.swiftboot.admin.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * File rename DTO.
 */
@Data
@Schema(description = "File rename request")
public class SysFileRenameDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "newName is required")
    @Schema(description = "New display name")
    private String newName;
}
