package com.shipping.booking_service.dto.Request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ReceiveRequest(@NotBlank @Pattern(regexp = "^\\d{6}$", message = "Code must be 6 digits") String otp,
                             boolean packagingOk, boolean labelOk,
                             @NotNull @DecimalMin("0.01") BigDecimal actualWeightKg, @Size(max = 255) String note) {}
