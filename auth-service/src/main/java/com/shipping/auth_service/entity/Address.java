package com.shipping.auth_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "address")
@Getter
@Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = "user")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long addressId;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @Column(name = "street",nullable = false,length = 100)
    @NotBlank(message = "Street Can not be null")
    @Size(min = 2,max = 100,message = "Street Size must be between 2 to 100")
    private String street;

    @Column(name = "city",nullable = false,length = 100)
    @NotBlank(message = "city Can not be null")
    @Pattern(
            regexp = "^[A-Za-z ]+$",
            message = "City should contain only letters"
    )
    private String city;

    @Column(name = "state",nullable = false,length = 100)
    @NotBlank(message = "state Can not be null")
    @Pattern(
            regexp = "^[A-Za-z ]+$",
            message = "Invalid state"
    )
    private String state;

    @Column(name = "country",nullable = false,length = 100)
    @NotBlank(message = "country Can not be null")
    @Pattern(
            regexp = "^[A-Za-z ]+$",
            message = "Invalid state"
    )
    private String country;

    @Column(name = "pincode",nullable = false,length = 10)
    @NotBlank(message = "pincode Can not be null")
    @Pattern(
            regexp = "^[1-9][0-9]{5}$",
            message = "Invalid Indian pincode"
    )
    private String pincode;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
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
