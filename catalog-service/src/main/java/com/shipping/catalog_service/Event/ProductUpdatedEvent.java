package com.shipping.catalog_service.Event;

import java.math.BigDecimal;

public record ProductUpdatedEvent(
        Long productId,
        Long ownerId,
        String productCode,
        String productName,
        String productDescription,
        BigDecimal productPrice,
        BigDecimal productWeight
) {}
