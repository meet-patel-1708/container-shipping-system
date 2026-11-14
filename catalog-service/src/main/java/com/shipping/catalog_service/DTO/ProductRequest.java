package com.shipping.catalog_service.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public record ProductRequest (

    @NotBlank(message = "Product code is required")
    @Pattern(regexp = "^PC[0-9]{5}$", message = "Product code must be like PC12345")
     String productCode,

    @NotBlank(message = "Product name is required")
     String productName,

    @NotBlank(message = "Product description is required")
     String productDescription,

    @NotNull(message = "Product price is required")
    @DecimalMin(value = "0.0", message = "Product price cannot be negative")
     BigDecimal productPrice,

    @NotNull(message = "Product weight is required")
    @DecimalMin(value = "0.0", message = "Product weight cannot be negative")
     BigDecimal productWeight,

     String productImageUrl,

     LocalDateTime createdAt,

     LocalDateTime updatedAt
){}
