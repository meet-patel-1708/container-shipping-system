package com.shipping.catalog_service.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(name = "product_code", nullable = false, length = 30)   // removed unique = true
    @NotBlank @Pattern(regexp = "^PC[0-9]{5}$", message = "Product code must be like PC12345")
    private String productCode;

    @Column(name = "owner_id", nullable = false, updatable = false)
    private Long ownerId;

    @Column(name = "product_name", nullable = false, length = 100)
    @NotBlank(message = "Product name is required")
    private String productName;

    @Column(name = "product_description", nullable = false, length = 500)
    @NotBlank(message = "Product description is required")
    private String productDescription;

    @Column(name = "product_price", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Product price is required")
    @DecimalMin(value = "0.0", message = "Product price cannot be negative")
    private BigDecimal productPrice;

    @Column(name = "product_weight", nullable = false, precision = 8, scale = 2)
    @NotNull(message = "Product weight is required")
    @DecimalMin(value = "0.0", message = "Product weight cannot be negative")
    private BigDecimal productWeight;

    @Column(name = "product_image_url", length = 500)
    private String productImageUrl;

    @Column(name = "created_at",nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
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