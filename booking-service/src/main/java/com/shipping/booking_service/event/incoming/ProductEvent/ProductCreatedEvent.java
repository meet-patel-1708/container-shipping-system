package com.shipping.booking_service.event.incoming.ProductEvent;

import java.math.BigDecimal;

public record ProductCreatedEvent(
        Long productId,
        String productCode,
        String productName,
        String productDescription,
        BigDecimal productPrice,
        BigDecimal productWeight
) {}