package com.shipping.auth_service.Config;

import com.shipping.auth_service.Security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter authFilter;

    private static final String[] PUBLIC_ENDPOINTS = {

            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/verify-email",
            "/api/auth/resend-verification",

            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/actuator/health"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity security
    ) throws Exception {

        security
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        (req, res, e) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED)))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                        .requestMatchers(
                                "/api/admin/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "SUPER_ADMIN"
                        )

                        .requestMatchers(
                                "/api/employee/**"
                        ).hasAnyRole(
                                "EMPLOYEE",
                                "ADMIN",
                                "SUPER_ADMIN"
                        )

                        .requestMatchers(
                                "/api/driver/**"
                        ).hasAnyRole(
                                "DRIVER",
                                "ADMIN",
                                "SUPER_ADMIN"
                        )

                        .requestMatchers(
                                "/api/user/**"
                        ).hasAnyRole(
                                "USER",
                                "ADMIN",
                                "SUPER_ADMIN"
                        )
                        .requestMatchers(
                                "/api/auth/login", "/api/auth/register", "/api/auth/verify-email",
                                "/api/auth/resend-verification", /* ...your other public paths... */
                                "/api/auth/accept-invite"          // add this
                        ).permitAll()
                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        authFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return security.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder(12);
    }
}