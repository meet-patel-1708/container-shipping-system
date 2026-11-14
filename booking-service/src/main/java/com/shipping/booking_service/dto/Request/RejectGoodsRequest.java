package com.shipping.booking_service.dto.Request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RejectGoodsRequest(boolean packagingOk, boolean labelOk,
                                 @NotNull @DecimalMin("0.01") BigDecimal actualWeightKg, @NotBlank @Size(max = 255) String reason) {}