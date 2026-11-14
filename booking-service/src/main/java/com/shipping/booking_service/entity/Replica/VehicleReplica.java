package com.shipping.booking_service.entity.Replica;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_replica")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleReplica {

    @Id
    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "vehicle_name", nullable = false, length = 20)
    private String vehicleName;

    @Column(name = "vehicle_number", nullable = false, length = 20)
    private String vehicleNumber;

    @Column(name = "vehicle_type", nullable = false, length = 20)
    private String vehicleType;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}