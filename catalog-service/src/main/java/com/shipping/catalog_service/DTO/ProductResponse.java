package com.shipping.catalog_service.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long productId,
        String productCode,
        String productName,
        String productDescription,
        BigDecimal productPrice,
        BigDecimal productWeight,
        String productImageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}