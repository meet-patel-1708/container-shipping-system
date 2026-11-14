package com.shipping.booking_service.dto.Response;

import com.shipping.booking_service.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(Long bookingId, Long containerId, Long userId, Long shipId,
                              Long originPortId, Long destPortId, BookingStatus status, BigDecimal totalAmount,
                              List<BookingItemResponse> items, LocalDateTime createdAt, LocalDateTime updatedAt,
                              BigDecimal totalWeightKg, LocalDateTime scheduleDate, BigDecimal containerRemainingKg) {}
