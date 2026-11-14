package com.shipping.fleet_service.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "vehicle")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_id")
    private Long id;

    @Column(name = "vehicle_name",nullable = false,length = 20)
    @NotBlank(message = "vehicle name must be exist")
    private String vehicleName;

    @Column(name = "vehicle_number",nullable = false,length = 20)
    @NotBlank(message = "vehicle number must be exist")
    private String vehicleNumber;

    @Column(name = "vehicle_capacity",nullable = false)
    @NotNull(message = "Vehicle Capacity must be greater than 0")
    private int vehicleCapacity;

    @Column(name = "vehicle_type",nullable = false)
    @NotNull(message = "vehicle Type is must be exist")
    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
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
