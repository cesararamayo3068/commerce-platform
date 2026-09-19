package com.portfolio.commerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Minimal CORS configuration for the Angular frontend.
 *
 * Allowed origins are resolved from the {@code cors.allowed-origins} property
 * (env var {@code CORS_ALLOWED_ORIGINS}), a comma-separated list. The wildcard
 * {@code *} is intentionally NOT supported: origins must be explicit.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final List<String> allowedOrigins;

    public WebConfig(@Value("${cors.allowed-origins:http://localhost:14200}") String allowedOrigins) {
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
