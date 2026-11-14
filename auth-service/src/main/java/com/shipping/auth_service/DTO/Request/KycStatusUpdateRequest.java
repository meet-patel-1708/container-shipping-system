package com.shipping.auth_service.DTO.Request;

import com.shipping.auth_service.entity.KycStatus;
import jakarta.validation.constraints.NotNull;

public record KycStatusUpdateRequest(

        @NotNull(message = "status is required")
        KycStatus status
) {
}
