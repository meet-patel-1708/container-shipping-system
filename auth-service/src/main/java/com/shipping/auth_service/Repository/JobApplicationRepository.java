package com.shipping.auth_service.Repository;

import com.shipping.auth_service.entity.ApplicantStatus;
import com.shipping.auth_service.entity.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    boolean existsByJob_IdAndUser_UserId(Long jobId, Long userId);
    List<JobApplication> findByUser_UserIdOrderByCreatedAtDesc(Long userId);
    Page<JobApplication> findByStatus(ApplicantStatus status, Pageable pageable);
    List<JobApplication> findByStatusAndJoiningDateLessThanEqual(ApplicantStatus status, java.time.LocalDate date);
}
