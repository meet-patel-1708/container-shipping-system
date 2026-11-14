package com.shipping.auth_service.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectRequest(@NotBlank(message = "reason is required")
                            @Size(max = 500, message = "reason must not exceed 500 characters")
                            String reason) {
}
