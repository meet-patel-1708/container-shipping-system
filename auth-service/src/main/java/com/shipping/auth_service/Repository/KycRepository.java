package com.shipping.auth_service.Repository;

import com.shipping.auth_service.entity.Kyc;
import com.shipping.auth_service.entity.KycStatus;
import com.shipping.auth_service.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KycRepository extends JpaRepository<Kyc,Long> {
    Optional<Kyc> findByUser_UserId(Long UserId);
    Page<Kyc> findByStatus(KycStatus status, Pageable pageable);
}
