package com.shipping.booking_service.entity.Replica;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_replica")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserReplica {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "phone", length = 15) // removed nullable = false - phone isn't in the register event
    private String phone;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}