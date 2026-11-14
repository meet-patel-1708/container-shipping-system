package com.shipping.booking_service.repository.Replica;

import com.shipping.booking_service.entity.Replica.ShipReplica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipReplicaRepository extends JpaRepository<ShipReplica, Long> {}