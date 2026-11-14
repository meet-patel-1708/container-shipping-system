package com.shipping.auth_service.DTO.Response;

import java.time.LocalDateTime;

public record UserResponse(
        Long userId, String name, String email, String phone,
        String role, String status,
        boolean emailVerified, boolean phoneVerified,
        String posting, Long assignedPortId,LocalDateTime createdAt, LocalDateTime updatedAt) {}