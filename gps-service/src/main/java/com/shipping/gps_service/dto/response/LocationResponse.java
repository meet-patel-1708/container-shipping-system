package com.shipping.gps_service.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public record LocationResponse (
        Long id,
        Long containerId,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal speedKmph,
        BigDecimal heading,
        Instant recordedAt
) implements Serializable {
}
