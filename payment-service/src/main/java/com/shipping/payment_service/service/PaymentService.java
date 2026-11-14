package com.shipping.payment_service.service;

import java.math.BigDecimal;

import com.shipping.payment_service.dto.request.FailPaymentRequest;
import com.shipping.payment_service.dto.request.VerifyPaymentRequest;
import com.shipping.payment_service.dto.response.CreateOrderResponse;
import com.shipping.payment_service.dto.response.PagedResponse;
import com.shipping.payment_service.dto.response.PaymentResponse;
import com.shipping.payment_service.entity.PaymentStatus;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    void createPendingPayment(Long bookingId, Long userId, BigDecimal amount);
    CreateOrderResponse createOrder(Long userId, Long paymentId);
    PaymentResponse verifyPayment(Long userId, Long paymentId, VerifyPaymentRequest request);
    PaymentResponse getById(Long userId, Long paymentId);
    PaymentResponse getByBookingId(Long userId, Long bookingId);
    PagedResponse<PaymentResponse> listMine(Long userId, PaymentStatus status, Pageable pageable);
    void handleBookingCancelled(Long bookingId);
    PaymentResponse reportFailure(Long userId, Long paymentId, FailPaymentRequest request);
    PagedResponse<PaymentResponse> listAll(PaymentStatus status, Pageable pageable);

}
