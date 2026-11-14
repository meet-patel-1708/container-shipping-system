package com.shipping.booking_service.dto.Response;

import java.math.BigDecimal;

public record PortExternalResponse(Long id, String portName, String portCode,
                                   BigDecimal latitude, BigDecimal longitude) {}