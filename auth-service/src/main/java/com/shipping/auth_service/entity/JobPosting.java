package com.shipping.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_posting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPosting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "job_id") private Long id;
    @Column(name = "title", nullable = false, length = 100) private String title;
    @Column(name = "posting", nullable = false, length = 50) private String posting;
    @Column(name = "description", nullable = false, length = 2000) private String description;
    @Column(name = "port_id") private Long portId;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false, length = 10) private JobStatus status;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
}
