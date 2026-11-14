package com.shipping.booking_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "port_inspection")
@Getter
@Setter
@NoArgsConstructor
public class PortInspection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "inspection_id") private Long id;
    @Column(name = "booking_id", nullable = false) private Long bookingId;
    @Column(name = "accepted", nullable = false) private boolean accepted;
    @Column(name = "packaging_ok", nullable = false) private boolean packagingOk;
    @Column(name = "label_ok", nullable = false) private boolean labelOk;
    @Column(name = "actual_weight_kg", nullable = false, precision = 12, scale = 2) private BigDecimal actualWeightKg;
    @Column(name = "note", length = 255) private String note;
    @Column(name = "inspected_by") private Long inspectedBy;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
}

