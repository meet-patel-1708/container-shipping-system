package com.shipping.booking_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final String PREFIX = "Bearer ";

    private final JwtService service;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(PREFIX)) { chain.doFilter(request, response); return; }
        try {
            Claims claims = service.extractClaims(header.substring(PREFIX.length()).trim());
            String role = claims.get("role", String.class);
            if (!"access".equals(claims.get("type", String.class)) || role == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                var principal = new UserPrinciple(Long.valueOf(claims.getSubject()),
                        claims.get("email", String.class));

                List<GrantedAuthority> auths = new ArrayList<>();   // local, per request
                auths.add(new SimpleGrantedAuthority("ROLE_" + role));
                Object port = claims.get("portId");
                if (port != null) auths.add(new SimpleGrantedAuthority("PORT_" + port));

                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(principal, null, auths));
            }
        } catch (JwtException | IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        chain.doFilter(request, response);
    }
}