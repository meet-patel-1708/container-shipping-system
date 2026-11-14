package com.shipping.fleet_service.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ship")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ship_id")
    private Long id;

    @Column(name = "ship_company_name",nullable = false,length = 50)
    @NotBlank(message = "Ship Company Name Is Required")
    private String shipCompanyName;

    @Column(name = "ship_code",nullable = false,length = 50)
    @NotBlank(message = "Ship code Name Is Required")
    @Pattern(regexp = "^SH[0-9]{5}$",message = "ShipCode must be starts with sh and then digits atleast 5")
    private String shipCode;

    @Column(name = "ship_Capacity",nullable = false,length = 50)
    @NotNull(message = "Ship capacity Name Is Required")
    private Long shipCapacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "port_id", nullable = false)
    private Port port;

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
