package com.shipping.booking_service.entity.Replica;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "port_replica")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortReplica {

    @Id
    @Column(name = "port_id")
    private Long portId;

    @Column(name = "port_name", nullable = false, length = 100)
    private String portName;

    @Column(name = "port_code", nullable = false, length = 10)
    private String portCode;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}