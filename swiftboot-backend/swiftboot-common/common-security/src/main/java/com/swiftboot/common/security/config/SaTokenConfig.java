package com.swiftboot.common.security.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册 Sa-Token 拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 登录校验 -- 拦截所有路由
            SaRouter.match("/**")
                    // 排除登录接口
                    .notMatch("/auth/login", "/auth/logout", "/auth/captcha", "/auth/encrypt/**")
                    // 排除静态资源
                    .notMatch("/favicon.ico", "/error")
                    // 排除 Swagger/Knife4j
                    .notMatch("/doc.html", "/webjars/**", "/swagger-resources/**", "/v3/api-docs/**")
                    // 排除文件访问
                    .notMatch("/files/**")
                    // 排除 Python 引擎回调的内部接口
                    .notMatch("/monitor/operlog/inner/add")
                    // 检查是否登录
                    .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }
}
