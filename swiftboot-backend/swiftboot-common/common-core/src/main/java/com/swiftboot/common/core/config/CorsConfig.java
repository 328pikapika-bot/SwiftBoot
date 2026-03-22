package com.swiftboot.common.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${swiftboot.security.cors.allowed-origin-patterns:http://localhost:30328,http://127.0.0.1:30328}")
    private String allowedOriginPatterns;

    @Value("${swiftboot.security.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${swiftboot.security.cors.allowed-headers:Authorization,Content-Type,X-Requested-With}")
    private String allowedHeaders;

    @Value("${swiftboot.security.cors.exposed-headers:Authorization}")
    private String exposedHeaders;

    @Value("${swiftboot.security.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        splitProperty(allowedOriginPatterns).forEach(config::addAllowedOriginPattern);
        splitProperty(allowedHeaders).forEach(config::addAllowedHeader);
        splitProperty(allowedMethods).forEach(config::addAllowedMethod);
        splitProperty(exposedHeaders).forEach(config::addExposedHeader);
        config.setAllowCredentials(allowCredentials);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    private List<String> splitProperty(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }
}
