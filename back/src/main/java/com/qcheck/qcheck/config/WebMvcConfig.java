package com.qcheck.qcheck.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 웹 설정
 *
 * 책임:
 * - CORS (Cross-Origin Resource Sharing) 설정
 * - 프론트엔드(localhost:4200)에서 백엔드(localhost:8081)로의 요청 허용
 *
 * CORS 정책:
 * - 허용 출처: http://localhost:4200 (Angular 개발 서버)
 * - 허용 메서드: GET, POST, PUT, DELETE, OPTIONS
 * - 허용 헤더: * (모든 헤더)
 * - 인증 정보 포함: true (쿠키, Authorization 헤더 등)
 * - 캐시 시간: 3600초 (1시간)
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods}")
    private String allowedMethods;

    @Value("${cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${cors.allow-credentials}")
    private boolean allowCredentials;

    @Value("${cors.max-age}")
    private long maxAge;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods(allowedMethods.split(","))
                .allowedHeaders(allowedHeaders.split(","))
                .allowCredentials(allowCredentials)
                .maxAge(maxAge);
    }
}
