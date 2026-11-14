package com.shipping.booking_service.dto.Response;

import java.math.BigDecimal;

public record ProductExternalResponse(Long productId, String productCode, String productName, BigDecimal productPrice) {}
