package com.shipping.auth_service.DTO.Response;

import java.time.LocalDateTime;

public record StaffResponse(Long userId, String name, String email, String phone,
                            String role, boolean emailVerified,String posting,Long assignedPortId, LocalDateTime createdAt) {}