package com.shipping.booking_service.repository.Replica;

import com.shipping.booking_service.entity.Replica.VehicleReplica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleReplicaRepository extends JpaRepository<VehicleReplica, Long> {}