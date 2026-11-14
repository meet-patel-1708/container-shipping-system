package com.shipping.auth_service.Repository;

import com.shipping.auth_service.entity.Resignation;
import com.shipping.auth_service.entity.ResignationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ResignationRepository extends JpaRepository<Resignation, Long> {
    Optional<Resignation> findFirstByUser_UserIdOrderByRequestedAtDesc(Long userId);
    Optional<Resignation> findByUser_UserIdAndStatusIn(Long userId, Collection<ResignationStatus> statuses);
    Page<Resignation> findByStatus(ResignationStatus status, Pageable pageable);
    List<Resignation> findByStatusAndLastWorkingDateBefore(ResignationStatus status, LocalDate date);
}
