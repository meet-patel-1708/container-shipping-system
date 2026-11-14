package com.shipping.payment_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id",unique = true)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "booking_id",nullable = false)
    @NotNull(message = "Booking id is mandatory")
    private Long bookingId;

    @Column(name = "amount",nullable = false,precision = 10,scale = 2)
    @NotNull(message = "anount is not 0")
    private BigDecimal amount;

    @Column(name = "currency",nullable = false,length = 5)
    @NotBlank(message = "currency is not empty")
    private String currency;

    @Column(name = "payment_status",nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(name = "payment_method",nullable = false,length = 30)
    @NotBlank(message = "payment method is not emoty")
    private String paymentMethod;

    @Column(name = "transaction_ref",unique = true,length = 100)
    private String transactionReference;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "razorpay_order_id", length = 50)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", length = 50)
    private String razorpayPaymentId;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void create(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

    }
    @PreUpdate
    public void update(){
        this.updatedAt = LocalDateTime.now();
    }
}
