package com.shipping.payment_service.mapper;

import com.shipping.payment_service.dto.response.PaymentResponse;
import com.shipping.payment_service.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    public PaymentResponse toResponse(Payment payment){
        return new PaymentResponse(
                payment.getId(),
                payment.getBookingId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentStatus(),
                payment.getPaymentMethod(),
                payment.getTransactionReference(),
                payment.getPaidAt(),
                payment.getCreatedAt(),
                payment.getUpdatedAt(),
                payment.getUserId()
        );
    }
}
