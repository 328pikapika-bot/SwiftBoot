package com.swiftboot.common.log.annotation;

import com.swiftboot.common.log.enums.BusinessType;

import java.lang.annotation.*;

/**
 * 操作日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /**
     * 模块名称
     */
    String title() default "";

    /**
     * 业务类型
     */
    BusinessType businessType() default BusinessType.OTHER;

    /**
     * 是否保存请求参数
     */
    boolean saveRequestData() default true;

    /**
     * 是否保存响应数据
     */
    boolean saveResponseData() default true;
}
