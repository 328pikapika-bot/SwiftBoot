package com.swiftboot;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

import java.net.InetAddress;

/**
 * SwiftBoot 后台管理启动类
 */
@Slf4j
@EnableAsync
@SpringBootApplication
@ComponentScan(basePackages = {"com.swiftboot"})
@MapperScan("com.swiftboot.**.mapper")
public class SwiftBootAdminApplication {

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = SpringApplication.run(SwiftBootAdminApplication.class, args);
        Environment env = context.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        
        log.info("""
                
                ----------------------------------------------------------
                \tSwiftBoot 启动成功！
                \t本地访问: \thttp://localhost:{}{}
                \t外部访问: \thttp://{}:{}{}
                \t接口文档: \thttp://localhost:{}{}/doc.html
                ----------------------------------------------------------
                """, port, contextPath, ip, port, contextPath, port, contextPath);
    }
}
