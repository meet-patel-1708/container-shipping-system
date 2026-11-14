package com.shipping.auth_service.DTO.Response;

public record RegisterResponse(Long userId,
                               String name,
                               String email,
                               String phone,
                               String role,
                               String status,
                               String message) {
}
