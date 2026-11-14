package com.shipping.api_gateway.filter;

import com.shipping.api_gateway.config.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpHeaders;

import org.springframework.core.io.buffer.DataBuffer;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationGlobalFilter implements GlobalFilter, Ordered {
    private static final String BEARER_PREFIX = "Bearer";

    private final JwtValidator validator;
    private final SecurityProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain){
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if(HttpMethod.OPTIONS.equals(request.getMethod()) || isPublic(path)){
            return chain.filter(exchange);
        }
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange, "Missing or malformed Authorization header");
        }
        try{
            Claims claims = validator.parse(header.substring(BEARER_PREFIX.length()).trim());
            if(!"access".equals(claims.get("type",String.class))){
                return unauthorized(exchange,"Access token required");
            }
        }catch (JwtException | IllegalArgumentException e){
            log.debug("Rejected request to{}:{}",path,e.getMessage());
            return unauthorized(exchange,"Invalid or expired token");
        }
        return chain.filter(exchange);
    }
    private boolean isPublic(String path){
        return properties.getPublicPaths().stream().anyMatch(pattern->pathMatcher.match(pattern,path));
    }
    private Mono<Void> unauthorized(ServerWebExchange exchange,String message){
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
    @Override
    public int getOrder(){
        return -1;
    }
}
