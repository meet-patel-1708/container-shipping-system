package com.shipping.auth_service.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_application")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"job", "user"})
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "application_id") private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "job_id", nullable = false) private JobPosting job;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(name = "resume_key", nullable = false, length = 255) private String resumeKey;
    @Column(name = "resume_name", nullable = false, length = 255) private String resumeName;
    @Column(name = "content_type", nullable = false, length = 100) private String contentType;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false, length = 12) private ApplicantStatus status;
    @Column(name = "onboarding_port_id") private Long onboardingPortId;
    @Column(name = "joining_date")
    private java.time.LocalDate joiningDate;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}

