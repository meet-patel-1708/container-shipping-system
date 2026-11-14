
package com.shipping.gps_service.event;

import java.io.Serializable;
import java.math.BigDecimal;

public record LocationPingEvent(
        Long containerId,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal speedKmph,
        BigDecimal heading
) implements Serializable {
}
