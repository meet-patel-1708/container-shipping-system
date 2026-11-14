package com.shipping.booking_service.entity.Replica;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ship_replica")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipReplica {

    @Id
    @Column(name = "ship_id")
    private Long shipId;

    @Column(name = "ship_company_name", nullable = false, length = 50)
    private String shipCompanyName;

    @Column(name = "ship_code", nullable = false, length = 50)
    private String shipCode;

    @Column(name = "port_id", nullable = false)
    private Long portId;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}