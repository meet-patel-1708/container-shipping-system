package com.shipping.gps_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "container_location",indexes = {
        @Index(
                name = "idx_location_container_recorded",
                columnList = "container_id, recorded_at DESC"
        ),
        @Index(
                name = "idx_location_recorded_at",
                columnList = "recorded_at DESC"
        )
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContainerLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "container_id",nullable = false)
    private Long containerId;

    @Column(name = "latitude",nullable = false,precision = 9,scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude",nullable = false,precision = 9,scale = 6)
    private BigDecimal longitude;

    @Column(name = "speed_kmph",precision = 9,scale = 6)
    private BigDecimal speedKmph;

    @Column(
            name = "heading",
            precision = 5,
            scale = 2
    )
    private BigDecimal heading;

    @Column(
            name = "recorded_at",
            nullable = false
    )
    private OffsetDateTime recordedAt;
}
