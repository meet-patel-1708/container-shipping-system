package com.shipping.gps_service.repository;

import com.shipping.gps_service.entity.ContainerLocation;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContainerLocationRepository extends JpaRepository<ContainerLocation,Long> {
    Page<ContainerLocation> findByContainerIdOrderByRecordedAtDesc(Long containerId, Pageable pageable);

    @Query("""
    SELECT cl FROM ContainerLocation cl
    WHERE cl.containerId = :containerId
    ORDER BY cl.recordedAt DESC
    LIMIT 1
    """)
    Optional<ContainerLocation> findLatestByContainerId(
            @Param("containerId") Long containerId
    );
}
