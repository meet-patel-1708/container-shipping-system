package com.shipping.auth_service.Security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME =
            "Authorization";

    private static final String PREFIX =
            "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String header =
                request.getHeader(HEADER_NAME);

        if (header == null ||
                !header.startsWith(PREFIX)) {

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                header.substring(PREFIX.length());

        Claims claims =
                jwtService.extractClaims(token);
        if (claims != null
                && "access".equals(claims.get("type", String.class))
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String subject = claims.getSubject();
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);

            if (subject == null || role == null) {
                filterChain.doFilter(request, response);
                return;
            }

            Long userId;
            try {
                userId = Long.valueOf(subject);
            } catch (NumberFormatException e) {
                filterChain.doFilter(request, response);
                return;
            }

             userId =
                    Long.valueOf(subject);

            UserPrinciple principal =
                    new UserPrinciple(
                            userId,
                            email
                    );

            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority(
                            "ROLE_" + role
                    );

            UsernamePasswordAuthenticationToken
                    authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            List.of(authority)
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}