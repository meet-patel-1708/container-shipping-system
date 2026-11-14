package com.shipping.payment_service.service.impl;

import com.shipping.payment_service.dto.request.FailPaymentRequest;
import com.shipping.payment_service.dto.request.VerifyPaymentRequest;
import com.shipping.payment_service.dto.response.CreateOrderResponse;
import com.shipping.payment_service.dto.response.PagedResponse;
import com.shipping.payment_service.dto.response.PaymentResponse;
import com.shipping.payment_service.entity.Payment;
import com.shipping.payment_service.entity.PaymentStatus;
import com.shipping.payment_service.event.PaymentEventPublisher;
import com.shipping.payment_service.event.outgoing.PaymentFailedEvent;
import com.shipping.payment_service.event.outgoing.PaymentSuccededEvent;
import com.shipping.payment_service.exception.PaymentAlreadyProcessException;
import com.shipping.payment_service.exception.PaymentNotFoundException;
import com.shipping.payment_service.exception.PaymentVerificationException;
import com.shipping.payment_service.gateway.RazorpayGateway;
import com.shipping.payment_service.mapper.PaymentMapper;
import com.shipping.payment_service.repository.PaymentRepository;
import com.shipping.payment_service.service.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository repository;
    private final PaymentMapper mapper;
    private final PaymentEventPublisher publisher;
    private final RazorpayGateway gateway;

    @Override
    @Transactional
    public PagedResponse<PaymentResponse> listAll(PaymentStatus status, Pageable pageable) {
        Page<Payment> page = status == null ? repository.findAll(pageable)
                : repository.findByPaymentStatus(status, pageable);
        return PagedResponse.from(page.map(mapper::toResponse));
    }
    @Override
    @Transactional
    public void createPendingPayment(Long bookingId,Long userId, BigDecimal amount) {
            if(repository.existsByBookingId(bookingId)){
                return;
            }
            Payment payment = new Payment();
            payment.setBookingId(bookingId);
            payment.setUserId(userId);
            payment.setAmount(amount);
            payment.setCurrency("INR");
            payment.setPaymentStatus(PaymentStatus.PENDING);
            payment.setPaymentMethod("UNSPECIFIED");
            repository.save(payment);
    }

    @Override
    @Transactional
    public CreateOrderResponse createOrder(Long userId, Long paymentId) {
        Payment p = repository.findForUpdate(paymentId, userId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        if (p.getPaymentStatus() == PaymentStatus.SUCCESS || p.getPaymentStatus() == PaymentStatus.REFUNDED) {
            throw new PaymentAlreadyProcessException(p.getBookingId());
        }
        if (p.getPaymentStatus() == PaymentStatus.FAILED) {      // retry after a failed attempt
            p.setPaymentStatus(PaymentStatus.PENDING);
            p.setFailureReason(null);
            repository.save(p);
        }
        if (p.getRazorpayOrderId() == null) {            // reuse the order if the user reopens checkout
            long paise = p.getAmount().movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
            p.setRazorpayOrderId(gateway.createOrder(paise, "bk-" + p.getBookingId()));
            repository.save(p);
        }
        long paise = p.getAmount().movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
        return new CreateOrderResponse(p.getId(), p.getBookingId(), p.getRazorpayOrderId(),
                paise, p.getCurrency(), gateway.keyId());
    }

    @Override
    @Transactional
    @CacheEvict(value = "payments", key = "#userId + '-' + #paymentId")
    public PaymentResponse verifyPayment(Long userId, Long paymentId, VerifyPaymentRequest req) {
        Payment p = repository.findForUpdate(paymentId, userId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        if (p.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new PaymentAlreadyProcessException(p.getBookingId());
        }
        if (p.getRazorpayOrderId() == null || !p.getRazorpayOrderId().equals(req.razorpayOrderId())) {
            throw new PaymentVerificationException("Order does not belong to this payment");
        }
        if (!gateway.verify(req.razorpayOrderId(), req.razorpayPaymentId(), req.razorpaySignature())) {
            throw new PaymentVerificationException("Payment signature verification failed");
        }
        if (p.getPaymentStatus() == PaymentStatus.SUCCESS || p.getPaymentStatus() == PaymentStatus.REFUNDED) {
            throw new PaymentAlreadyProcessException(p.getBookingId());
        }
        p.setFailureReason(null);

        p.setPaymentMethod("RAZORPAY");
        p.setPaymentStatus(PaymentStatus.SUCCESS);
        p.setRazorpayPaymentId(req.razorpayPaymentId());
        p.setTransactionReference(req.razorpayPaymentId());
        p.setPaidAt(LocalDateTime.now());
        Payment saved = repository.save(p);

        // publish only after the DB commit, so a rollback can never leave a "paid" event behind
        var event = new PaymentSuccededEvent(saved.getId(), saved.getBookingId(),
                saved.getUserId(), saved.getTransactionReference());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { publisher.publishPaymentSucceeded(event); }
        });
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    @Cacheable(value = "payments", key = "#userId + '-' + #paymentId")
    public PaymentResponse getById(Long userId, Long paymentId) {
        return mapper.toResponse(repository.findByIdAndUserId(paymentId, userId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId)));
    }

    @Override
    @Transactional
    public PaymentResponse getByBookingId(Long userId, Long bookingId) {
        return mapper.toResponse(repository.findByBookingIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new PaymentNotFoundException(bookingId)));
    }

    @Override
    @Transactional
    public PagedResponse<PaymentResponse> listMine(Long userId, PaymentStatus status, Pageable pageable) {
        Page<Payment> page = status == null
                ? repository.findByUserId(userId, pageable)
                : repository.findByUserIdAndPaymentStatus(userId, status, pageable);
        return PagedResponse.from(page.map(mapper::toResponse));
    }

    @Override
    @Transactional
    @CacheEvict(value = "payments", allEntries = true)
    public void handleBookingCancelled(Long bookingId) {
        repository.findByBookingId(bookingId).ifPresent(p -> {
            if (p.getPaymentStatus() == PaymentStatus.PENDING) p.setPaymentStatus(PaymentStatus.FAILED);
            else if (p.getPaymentStatus() == PaymentStatus.SUCCESS) p.setPaymentStatus(PaymentStatus.REFUNDED);
            repository.save(p);
        });
    }
    @Override
    @Transactional
    @CacheEvict(value = "payments", key = "#userId + '-' + #paymentId")
    public PaymentResponse reportFailure(Long userId, Long paymentId, FailPaymentRequest req) {
        Payment p = repository.findForUpdate(paymentId, userId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        // a failure report can never overwrite a verified success or a refund
        if (p.getPaymentStatus() != PaymentStatus.PENDING) {
            return mapper.toResponse(p);
        }
        if (p.getRazorpayOrderId() == null || !p.getRazorpayOrderId().equals(req.razorpayOrderId())) {
            throw new PaymentVerificationException("Order does not belong to this payment");
        }

        String reason = (req.reason() == null || req.reason().isBlank()) ? "Payment failed" : req.reason();
        p.setPaymentStatus(PaymentStatus.FAILED);
        p.setFailureReason(reason);
        if (req.razorpayPaymentId() != null) p.setRazorpayPaymentId(req.razorpayPaymentId());
        Payment saved = repository.save(p);

        var event = new PaymentFailedEvent(saved.getId(), saved.getBookingId(), saved.getUserId(), reason);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { publisher.publishPaymentFailed(event); }
        });
        return mapper.toResponse(saved);
    }
}
