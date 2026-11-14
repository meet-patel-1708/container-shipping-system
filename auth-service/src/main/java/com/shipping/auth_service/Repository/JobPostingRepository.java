package com.shipping.auth_service.Repository;

import com.shipping.auth_service.entity.JobPosting;
import com.shipping.auth_service.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    List<JobPosting> findByStatusOrderByCreatedAtDesc(JobStatus status);
    List<JobPosting> findAllByOrderByCreatedAtDesc();
}
