package com.shipping.payment_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FailPaymentRequest(
        @NotBlank String razorpayOrderId,
        String razorpayPaymentId,
        @Size(max = 255) String reason) {}
