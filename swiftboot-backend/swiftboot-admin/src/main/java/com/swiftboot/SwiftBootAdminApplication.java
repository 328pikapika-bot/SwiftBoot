package com.swiftboot;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@EnableAsync
@EnableScheduling
@SpringBootApplication
@ComponentScan(basePackages = {"com.swiftboot"})
@MapperScan("com.swiftboot.**.mapper")
public class SwiftBootAdminApplication {

    public static void main(String[] args) throws Exception {
        loadQuickStartConfig();
        ConfigurableApplicationContext context = SpringApplication.run(SwiftBootAdminApplication.class, args);
        Environment env = context.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        boolean apiDocsEnabled = Boolean.parseBoolean(env.getProperty("springdoc.api-docs.enabled", "false"));
        boolean swaggerPublicAccessEnabled = Boolean.parseBoolean(env.getProperty("swiftboot.security.swagger.public-access-enabled", "false"));

        log.info("""

                ----------------------------------------------------------
                \tSwiftBoot started successfully
                \tLocal: \thttp://localhost:{}{}
                \tRemote: \thttp://{}:{}{}
                ----------------------------------------------------------
                """, port, contextPath, ip, port, contextPath);

        if (apiDocsEnabled) {
            log.info("API docs{}: http://localhost:{}{}/doc.html",
                    swaggerPublicAccessEnabled ? "" : " (login required)",
                    port,
                    contextPath);
        }
    }

    private static void loadQuickStartConfig() {
        Path configPath = resolveStartConfigPath();
        if (configPath == null) {
            return;
        }

        Map<String, String> loadedKeys = new LinkedHashMap<>();
        try {
            List<String> lines = Files.readAllLines(configPath, StandardCharsets.UTF_8);
            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith(";") || line.startsWith("#")) {
                    continue;
                }

                int index = line.indexOf('=');
                if (index < 0) {
                    continue;
                }

                String key = line.substring(0, index).trim();
                String value = line.substring(index + 1).trim();
                if (value.isEmpty()) {
                    continue;
                }

                switch (key) {
                    case "DB_PASSWORD" -> applySystemPropertyIfMissing("SWIFTBOOT_DB_PASSWORD", value, loadedKeys);
                    case "REDIS_PASSWORD" -> applySystemPropertyIfMissing("SWIFTBOOT_REDIS_PASSWORD", value, loadedKeys);
                    case "DEEPSEEK_API_KEY" -> applySystemPropertyIfMissing("SWIFTBOOT_DEEPSEEK_API_KEY", value, loadedKeys);
                    default -> {
                    }
                }
            }

            if (!loadedKeys.isEmpty()) {
                log.info("Loaded startup secrets from {}", configPath);
            }
        } catch (IOException e) {
            log.warn("Failed to read start_config.ini: {}", configPath, e);
        }
    }

    private static void applySystemPropertyIfMissing(String propertyName, String value, Map<String, String> loadedKeys) {
        if (System.getenv(propertyName) != null || System.getProperty(propertyName) != null) {
            return;
        }
        System.setProperty(propertyName, value);
        loadedKeys.put(propertyName, "loaded");
    }

    private static Path resolveStartConfigPath() {
        Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        for (int i = 0; i < 6 && current != null; i++) {
            Path quickStartCandidate = current.resolve("quick-start").resolve("start_config.ini");
            if (Files.isRegularFile(quickStartCandidate)) {
                return quickStartCandidate;
            }

            Path legacyCandidate = current.resolve("快速启动").resolve("start_config.ini");
            if (Files.isRegularFile(legacyCandidate)) {
                return legacyCandidate;
            }
            current = current.getParent();
        }
        return null;
    }
}
