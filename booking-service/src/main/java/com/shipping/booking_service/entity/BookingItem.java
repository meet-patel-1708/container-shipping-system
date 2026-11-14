package com.shipping.booking_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    // Real FK - booked_container lives in THIS database
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    @NotNull(message = "Booking is required")
    private BookedContainer booking;

    // Cross-service reference - owned by catalog-service, plain column, no JPA relation.
    // Validated at the application layer (Feign call + circuit breaker, or trust from
    // the Kafka-replicated local product_replica table) before insert.
    @Column(name = "product_id", nullable = false)
    @NotNull(message = "Product id is required")
    private Long productId;

    @Column(name = "quantity", nullable = false)
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    // Snapshot of the product's price AT BOOKING TIME - deliberately NOT looked up live
    // from catalog-service on every read. If catalog changes product_price next month,
    // this booking's total must stay historically accurate.
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", message = "Unit price cannot be negative")
    private BigDecimal unitPrice;
    @Column(name = "product_name", length = 150) private String productName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void create() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void update() {
        this.updatedAt = LocalDateTime.now();
    }
}