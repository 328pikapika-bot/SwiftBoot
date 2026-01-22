package com.swiftboot.admin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j 配置
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SwiftBoot API")
                        .description("SwiftBoot 轻量级后台管理系统接口文档")
                        .version("1.0.0")
                        .contact(new Contact().name("SwiftBoot"))
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
                .schemaRequirement("Authorization", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization"))
                .addSecurityItem(new SecurityRequirement().addList("Authorization"));
    }
}
