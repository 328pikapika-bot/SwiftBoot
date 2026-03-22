package com.swiftboot.common.security.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Value("${swiftboot.security.swagger.public-access-enabled:false}")
    private boolean swaggerPublicAccessEnabled;

    @Value("${swiftboot.security.files.public-access-enabled:true}")
    private boolean filePublicAccessEnabled;

    @Value("${swiftboot.security.files.system-file-access-public-enabled:true}")
    private boolean systemFileAccessPublicEnabled;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            var router = SaRouter.match("/**")
                    .notMatch("/auth/login", "/auth/logout", "/auth/captcha", "/auth/encrypt/**")
                    .notMatch("/favicon.ico", "/error");

            if (swaggerPublicAccessEnabled) {
                router = router.notMatch("/doc.html", "/swagger-ui.html", "/swagger-ui/**", "/webjars/**", "/swagger-resources/**", "/v3/api-docs/**");
            }
            if (filePublicAccessEnabled) {
                router = router.notMatch("/files/**");
            }
            if (systemFileAccessPublicEnabled) {
                router = router.notMatch("/system/file/access/**");
            }

            router.notMatch("/monitor/operlog/inner/add")
                    .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }
}
