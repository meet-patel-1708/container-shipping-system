package com.shipping.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "resignation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
public class Resignation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "resignation_id") private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(name = "reason", nullable = false, length = 500) private String reason;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false, length = 12) private ResignationStatus status;
    @Column(name = "notice_days") private Integer noticeDays;
    @Column(name = "notice_start_date") private LocalDate noticeStartDate;
    @Column(name = "last_working_date") private LocalDate lastWorkingDate;
    @CreationTimestamp
    @Column(name = "requested_at", nullable = false, updatable = false) private LocalDateTime requestedAt;
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
