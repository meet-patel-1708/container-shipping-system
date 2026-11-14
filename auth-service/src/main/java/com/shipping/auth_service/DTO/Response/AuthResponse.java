package com.shipping.auth_service.DTO.Response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInMs,
        UserResponse user
) {
    public static AuthResponse of(String accessToken,String refreshToken,String tokenType,long expiresInMs,UserResponse user){
        return new AuthResponse(accessToken,refreshToken,"Bearer",expiresInMs,user);
    }
}
