package com.shipping.payment_service.dto.response;

import com.shipping.payment_service.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id, Long bookingId, BigDecimal amount, String currency,
        PaymentStatus paymentStatus, String paymentMethod, String transactionReference,
        LocalDateTime paidAt, LocalDateTime createdAt, LocalDateTime updatedAt, Long userId) {}