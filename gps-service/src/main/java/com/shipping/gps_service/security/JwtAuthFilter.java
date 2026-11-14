package com.shipping.gps_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter  extends OncePerRequestFilter {
    private static final String HEADER_NAME="Authorization";
    private static final String PREFIX="Bearer";

    private final JwtService service;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header=request.getHeader(HEADER_NAME);
        if(header==null || !header.startsWith(PREFIX)){
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(PREFIX.length()).trim();
        try{
            Claims claims = service.extractClaims(token);
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                Long userId = Long.valueOf(claims.getSubject());
                String email = claims.get("email", String.class);
                UserPrinciple principal = new UserPrinciple(userId, email);
                var authToken = new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (JwtException | IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired JWT: " + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }
}
