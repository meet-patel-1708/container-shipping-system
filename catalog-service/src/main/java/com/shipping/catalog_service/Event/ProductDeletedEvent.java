package com.shipping.catalog_service.Event;

import java.math.BigDecimal;

public record ProductDeletedEvent(Long productId, Long ownerId) {}