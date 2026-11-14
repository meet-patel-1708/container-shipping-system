package com.shipping.fleet_service.Repository;

import com.shipping.fleet_service.Entity.Ship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipRepository extends JpaRepository<Ship, Long> {

    boolean existsByShipCode(String shipCode);

    Optional<Ship> findByShipCode(String shipCode);

    Page<Ship> findByShipCompanyNameContainingIgnoreCase(
            String shipCompanyName,
            Pageable pageable
    );
}