package com.swiftboot.common.core.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页结果
 *
 * @param <T> 数据类型
 */
@Data
@Schema(description = "分页结果")
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "数据列表")
    private List<T> list;

    @Schema(description = "总记录数")
    private long total;

    @Schema(description = "当前页码")
    private long pageNum;

    @Schema(description = "每页数量")
    private long pageSize;

    @Schema(description = "总页数")
    private long pages;

    public PageResult() {
    }

    public PageResult(List<T> list, long total, long pageNum, long pageSize) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = (total + pageSize - 1) / pageSize;
    }

    public static <T> PageResult<T> of(List<T> list, long total, long pageNum, long pageSize) {
        return new PageResult<>(list, total, pageNum, pageSize);
    }
}
