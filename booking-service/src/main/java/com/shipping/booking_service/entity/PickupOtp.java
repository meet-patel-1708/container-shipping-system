package com.shipping.booking_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "pickup_otp")
@Getter
@Setter
@NoArgsConstructor
public class PickupOtp {
    @Id
    @Column(name = "booking_id") private Long bookingId;
    @Column(name = "otp_hash", nullable = false, length = 64) private String otpHash;
    @Column(name = "expires_at", nullable = false) private LocalDateTime expiresAt;
    @Column(name = "attempts", nullable = false) private int attempts;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
}
