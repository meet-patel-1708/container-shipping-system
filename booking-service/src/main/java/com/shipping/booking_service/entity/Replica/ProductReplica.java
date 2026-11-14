package com.shipping.booking_service.entity.Replica;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_replica")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductReplica {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_code", nullable = false, length = 30)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "product_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal productPrice;

    @Column(name = "product_weight", precision = 8, scale = 2)
    private BigDecimal productWeight;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}