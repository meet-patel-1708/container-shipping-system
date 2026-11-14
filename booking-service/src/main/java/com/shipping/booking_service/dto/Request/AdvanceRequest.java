package com.shipping.booking_service.dto.Request;

import com.shipping.booking_service.entity.BookingStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdvanceRequest(@NotNull BookingStatus status, @Size(max = 255) String note) {}
