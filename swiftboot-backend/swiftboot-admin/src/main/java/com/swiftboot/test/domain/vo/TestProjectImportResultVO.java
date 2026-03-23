package com.swiftboot.test.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 示例项目导入结果
 */
@Data
@Schema(description = "示例项目导入结果")
public class TestProjectImportResultVO {

    @Schema(description = "成功条数")
    private int successCount;

    @Schema(description = "更新条数")
    private int updateCount;

    @Schema(description = "失败条数")
    private int failureCount;

    @Schema(description = "失败明细")
    private List<String> failureMessages = new ArrayList<>();
}
