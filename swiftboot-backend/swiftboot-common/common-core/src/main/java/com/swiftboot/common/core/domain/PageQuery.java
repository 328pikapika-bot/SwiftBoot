package com.swiftboot.common.core.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分页查询参数
 */
@Data
@Schema(description = "分页查询参数")
public class PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "当前页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "排序字段")
    private String orderBy;

    @Schema(description = "排序方向 (asc/desc)")
    private String orderDirection = "desc";

    /**
     * 获取偏移量
     */
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }
}
