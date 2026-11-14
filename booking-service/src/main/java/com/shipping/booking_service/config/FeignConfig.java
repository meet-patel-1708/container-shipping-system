package com.shipping.booking_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    public feign.RequestInterceptor bearerForwarder() {
        return template -> {
            var attrs = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attrs instanceof org.springframework.web.context.request.ServletRequestAttributes sra) {
                String h = sra.getRequest().getHeader(org.springframework.http.HttpHeaders.AUTHORIZATION);
                if (h != null) template.header(org.springframework.http.HttpHeaders.AUTHORIZATION, h);
            }
        };
    }
}