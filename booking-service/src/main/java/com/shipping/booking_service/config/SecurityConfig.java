package com.shipping.booking_service.config;

import com.shipping.booking_service.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableKafka
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter filter;
    private static final String[] PUBLIC_ENDPOINTS={
            "/swagger-ui/**",
            "v3/api-docs/**",
            "/actuator/health"
    };
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity security) throws Exception{
        security.csrf(csrf->csrf.disable())
                .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/containers").hasAnyRole("ADMIN","SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/containers/**").hasAnyRole("ADMIN","SUPER_ADMIN")
                        .requestMatchers("/api/staff/**").hasAnyRole("EMPLOYEE", "ADMIN","SUPER_ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e.authenticationEntryPoint(
                        new org.springframework.security.web.authentication.HttpStatusEntryPoint(
                                org.springframework.http.HttpStatus.UNAUTHORIZED)))   // 401 triggers the Angular refresh
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        return security.build();
    }
}
