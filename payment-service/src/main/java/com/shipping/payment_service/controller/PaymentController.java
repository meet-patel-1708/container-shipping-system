package com.shipping.payment_service.controller;

import com.shipping.payment_service.dto.request.FailPaymentRequest;
import com.shipping.payment_service.dto.request.VerifyPaymentRequest;
import com.shipping.payment_service.dto.response.CreateOrderResponse;
import com.shipping.payment_service.dto.response.PagedResponse;
import com.shipping.payment_service.dto.response.PaymentResponse;
import com.shipping.payment_service.entity.PaymentStatus;
import com.shipping.payment_service.security.UserPrinciple;
import com.shipping.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;
    @PostMapping("/{paymentId}/fail")
    public ResponseEntity<PaymentResponse> fail(@AuthenticationPrincipal UserPrinciple me,
                                                @PathVariable Long paymentId, @Valid @RequestBody FailPaymentRequest body) {
        return ResponseEntity.ok(service.reportFailure(me.userId(), paymentId, body));
    }
    @GetMapping("/my")
    public ResponseEntity<PagedResponse<PaymentResponse>> mine(
            @AuthenticationPrincipal UserPrinciple me,
            @RequestParam(required = false) PaymentStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.listMine(me.userId(), status, pageable));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getById(@AuthenticationPrincipal UserPrinciple me, @PathVariable Long paymentId) {
        return ResponseEntity.ok(service.getById(me.userId(), paymentId));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<PaymentResponse> byBooking(@AuthenticationPrincipal UserPrinciple me, @PathVariable Long bookingId) {
        return ResponseEntity.ok(service.getByBookingId(me.userId(), bookingId));
    }

    @PostMapping("/{paymentId}/order")
    public ResponseEntity<CreateOrderResponse> order(@AuthenticationPrincipal UserPrinciple me,
                                                     @PathVariable Long paymentId) {
        return ResponseEntity.ok(service.createOrder(me.userId(), paymentId));
    }

    @PostMapping("/{paymentId}/verify")
    public ResponseEntity<PaymentResponse> verify(@AuthenticationPrincipal UserPrinciple me,
                                                  @PathVariable Long paymentId, @Valid @RequestBody VerifyPaymentRequest body) {
        return ResponseEntity.ok(service.verifyPayment(me.userId(), paymentId, body));
    }
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PagedResponse<PaymentResponse>> all(
            @RequestParam(required = false) PaymentStatus status,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        return ResponseEntity.ok(service.listAll(status, pageable));
    }
}