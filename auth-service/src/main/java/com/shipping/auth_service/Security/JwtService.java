package com.shipping.auth_service.Security;
import com.shipping.auth_service.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import io.jsonwebtoken.JwtException;
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
            @Value("${jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String generateAccessToken(User user) {
        return buildToken(user, accessTokenExpirationMs, "access");
    }

    public String generateRefreshToken(User user) {
        return buildToken(user, refreshTokenExpirationMs, "refresh");
    }

    private String buildToken(User user, long expirationMs, String tokenType) {
        Date now = new Date();
        var b = Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .claim("email", user.getEmail())
                .claim("type", tokenType)
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs));
        if (user.getAssignedPortId() != null) b.claim("portId", user.getAssignedPortId());
        return b.signWith(signingKey).compact();
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    public Claims extractClaims(String token) {
        try {
            return Jwts.parser().verifyWith(signingKey).build()
                    .parseSignedClaims(token).getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isValid(String token) {
        return extractClaims(token) != null;
    }

    public Long extractUserId(String token) {
        Claims claims = extractClaims(token);
        return claims == null ? null : Long.valueOf(claims.getSubject());
    }

    public String extractEmail(String token) {
        Claims claims = extractClaims(token);
        return claims == null ? null : claims.get("email", String.class);
    }
}

