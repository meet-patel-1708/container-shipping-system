package com.shipping.booking_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "container")
@AllArgsConstructor
@NoArgsConstructor
public class Container {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "container_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "container_type", nullable = false, length = 30)
    @NotNull(message = "Container type is required")
    private ContainerType containerType;

    @Column(name = "container_size", nullable = false)
    @NotNull(message = "Container size is required")
    private Integer containerSize;

    @Column(name = "container_company_name", nullable = false, length = 100)
    @NotBlank(message = "Container company name is required")
    @Length(min = 2, max = 100, message = "Container company name must be between 2 and 100 characters")
    private String containerCompanyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Status is required")
    private Status status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "capacity_kg", nullable = false, precision = 12, scale = 2)
    private BigDecimal capacityKg = BigDecimal.ZERO;

    @Column(name = "allocated_kg", nullable = false, precision = 12, scale = 2)
    private BigDecimal allocatedKg = BigDecimal.ZERO;

    public BigDecimal remainingKg() { return capacityKg.subtract(allocatedKg); }

    public void release(BigDecimal kg) {
        allocatedKg = allocatedKg.subtract(kg).max(BigDecimal.ZERO);
    }
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