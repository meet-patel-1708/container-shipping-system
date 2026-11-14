package com.shipping.booking_service.repository.Replica;

import com.shipping.booking_service.entity.Replica.ProductReplica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductReplicaRepository extends JpaRepository<ProductReplica, Long> {}