package com.shipping.gps_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;

@Component
@RequiredArgsConstructor
public class StompAuthInterceptor implements ChannelInterceptor {

    private static final Pattern TOPIC = Pattern.compile("^/topic/container/(\\d+)$");
    private final JwtService jwt;
    private final TrackingAccessService access;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (acc == null || acc.getCommand() == null) return message;

        switch (acc.getCommand()) {
            case CONNECT -> {
                String h = acc.getFirstNativeHeader("Authorization");
                if (h == null || !h.startsWith("Bearer ")) throw new MessagingException("Missing token");
                String token = h.substring(7).trim();
                try {
                    Claims c = jwt.extractClaims(token);
                    if (!"access".equals(c.get("type", String.class)))
                        throw new MessagingException("Access token required");
                    acc.getSessionAttributes().put("token", token);
                } catch (JwtException | IllegalArgumentException e) {
                    throw new MessagingException("Invalid token");
                }
            }
            case SUBSCRIBE -> {
                Matcher m = TOPIC.matcher(acc.getDestination() == null ? "" : acc.getDestination());
                String token = (String) acc.getSessionAttributes().get("token");
                if (!m.matches() || token == null || !access.canTrack(token, Long.valueOf(m.group(1))))
                    throw new MessagingException("Not allowed to track this container");
            }
            default -> { }
        }
        return message;
    }
}