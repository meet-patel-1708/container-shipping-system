package com.shipping.fleet_service.Repository;

import com.shipping.fleet_service.Entity.Vehicle;
import com.shipping.fleet_service.Entity.VehicleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    boolean existsByVehicleNumber(String vehicleNumber);

    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);

    Page<Vehicle> findByVehicleNameContainingIgnoreCase(
            String vehicleName,
            Pageable pageable
    );

    Page<Vehicle> findByVehicleType(
            VehicleType vehicleType,
            Pageable pageable
    );
}