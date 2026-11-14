package com.shipping.booking_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "booked_container")
@NoArgsConstructor
@AllArgsConstructor
public class BookedContainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "container_id", nullable = false)
    @NotNull(message = "Container is required")
    private Container container;

    @Column(name = "user_id", nullable = false)
    @NotNull(message = "User id is required")
    private Long userId;

    @Column(name = "ship_id", nullable = false)
    @NotNull(message = "Ship id is required")
    private Long shipId;

    @Column(name = "origin_port_id", nullable = false)
    @NotNull(message = "Origin port id is required")
    private Long originPortId;

    @Column(name = "dest_port_id", nullable = false)
    @NotNull(message = "Destination port id is required")
    private Long destPortId;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Status is required")
    private BookingStatus status;

    @Column(name = "total_weight_kg", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalWeightKg = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // NEW: the "other direction" - lets you call booking.getItems() to get all line items.
    // mappedBy = "booking" points to the field NAME in BookingItem that owns the relationship.
    // cascade ALL + orphanRemoval: save/delete a booking -> its items are saved/deleted too;
    // remove an item from this list -> it's deleted from the DB. Safe direction (parent->child).
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BookingItem> items = new ArrayList<>();
    @Column(name = "schedule_at", nullable = false) private LocalDateTime scheduleAt;
    @Column(name = "port_notified", nullable = false) private boolean portNotified = false;
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