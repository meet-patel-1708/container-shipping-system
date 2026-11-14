package com.shipping.booking_service.dto.Request;

import com.shipping.booking_service.dto.Request.BookingItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record BookingRequest(
        @NotNull Long containerId, @NotNull Long shipId,
        @NotNull Long originPortId, @NotNull Long destPortId,
        @NotEmpty @Valid List<BookingItemRequest> items,@NotNull @Future LocalDateTime scheduleAt) {}