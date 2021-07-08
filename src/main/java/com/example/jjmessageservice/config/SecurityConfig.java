package com.example.jjmessageservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

import static java.util.Collections.singletonList;

@Configuration
public class SecurityConfig {

    @Value("${app.origin}")
    private String prodOrigin;

    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        List<String> allowOrigins = singletonList(prodOrigin);
        config.setAllowedOriginPatterns(allowOrigins);
        return new CorsFilter(source);
    }
}