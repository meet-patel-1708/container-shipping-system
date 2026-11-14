package com.shipping.booking_service.dto.Response;

import java.math.BigDecimal;

public record BookingItemResponse(
        Long itemId,
        Long productId,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        String productName) {}
