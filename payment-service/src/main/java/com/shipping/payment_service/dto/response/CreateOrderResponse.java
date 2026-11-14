package com.shipping.payment_service.dto.response;

public record CreateOrderResponse(Long paymentId, Long bookingId, String orderId,
                                  Long amount, String currency, String keyId) {}