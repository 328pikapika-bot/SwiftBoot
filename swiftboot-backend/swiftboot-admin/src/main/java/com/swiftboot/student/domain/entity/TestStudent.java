package com.swiftboot.student.domain.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.swiftboot.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 测试学生表
 *
 * @author SwiftBoot_chenshuang
 * @date 2026-01-28 21:47:45
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("test_student")
@Schema(description = "测试学生表")
public class TestStudent extends BaseEntity {

    @Schema(description = "学生名称")
    private String studentName;

    @Schema(description = "年龄")
    private Integer age;

    @Schema(description = "性别")
    private String sex;

    @Schema(description = "生日")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime birthday;

}
