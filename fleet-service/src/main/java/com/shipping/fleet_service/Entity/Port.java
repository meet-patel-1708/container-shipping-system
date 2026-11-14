package com.shipping.fleet_service.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "port")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Port {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "port_id")
    private Long id;

    @Column(name = "port_name",nullable = false,length = 100)
    @NotBlank(message = "Port Name is mandatory")
    private String portName;

    @Column(name = "port_address",nullable = false,length = 100)
    @NotBlank(message = "Port Address is not blank")
    private String portAddress;

    @Column(name = "port_code",nullable = false,unique = true)
    @Pattern(regexp = "^PC[0-9]{5}$",message = "Port Code is not matched")
    private String portCode;
    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

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
