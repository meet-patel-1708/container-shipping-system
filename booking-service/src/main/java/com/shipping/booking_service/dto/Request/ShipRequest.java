package com.shipping.booking_service.dto.Request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ShipRequest(@NotEmpty List<Long> bookingIds, @Size(max = 255) String note) {}
