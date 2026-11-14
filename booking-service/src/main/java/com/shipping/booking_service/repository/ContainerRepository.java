package com.shipping.booking_service.repository;

import com.shipping.booking_service.entity.Container;
import com.shipping.booking_service.entity.Status;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContainerRepository extends JpaRepository<Container, Long> {

    Page<Container> findByStatus(Status status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Container c WHERE c.id = :containerId")
    Optional<Container> findByIdForUpdate(@Param("containerId") Long containerId);
    List<Container> findByAllocatedKgGreaterThan(BigDecimal kg);
}