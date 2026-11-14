package com.shipping.catalog_service.Repository;

import com.shipping.catalog_service.Entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndOwnerId(Long id, Long ownerId);

    Optional<Product> findByProductCodeAndOwnerId(String productCode, Long ownerId);

    boolean existsByProductCodeAndOwnerId(String productCode, Long ownerId);

    Page<Product> findByOwnerId(Long ownerId, Pageable pageable);

    Page<Product> findByOwnerIdAndProductNameContainingIgnoreCase(
            Long ownerId, String name, Pageable pageable);
}