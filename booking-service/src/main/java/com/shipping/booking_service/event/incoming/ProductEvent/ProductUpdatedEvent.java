package com.shipping.booking_service.event.incoming.ProductEvent;

import java.math.BigDecimal;

public record ProductUpdatedEvent(Long productId, String productCode, String productName, BigDecimal productPrice, BigDecimal productWeight) {}

