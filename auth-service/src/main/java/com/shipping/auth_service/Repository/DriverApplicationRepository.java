package com.shipping.auth_service.Repository;

import com.shipping.auth_service.entity.ApplicantStatus;
import com.shipping.auth_service.entity.DriverApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverApplicationRepository extends JpaRepository<DriverApplication,Long> {
    Optional<DriverApplication> findByUser_UserId(Long userId);
    Page<DriverApplication> findByStatus(ApplicantStatus status, Pageable pageable);
}

