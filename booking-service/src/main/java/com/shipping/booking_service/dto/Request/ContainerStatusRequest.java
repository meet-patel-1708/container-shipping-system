package com.shipping.booking_service.dto.Request;

import com.shipping.booking_service.entity.Status;
import jakarta.validation.constraints.NotNull;

public record ContainerStatusRequest(@NotNull(message = "status is required")Status status) {
}
