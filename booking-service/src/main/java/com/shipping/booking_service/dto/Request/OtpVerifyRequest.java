package com.shipping.booking_service.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OtpVerifyRequest(@NotBlank @Pattern(regexp = "^\\d{6}$", message = "Code must be 6 digits") String otp) {}
