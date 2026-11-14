package com.shipping.auth_service.DTO.Response;

import com.shipping.auth_service.entity.KycStatus;
import com.shipping.auth_service.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public record KycResponse(
        Long kycId,
        Long userId,
        KycStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}