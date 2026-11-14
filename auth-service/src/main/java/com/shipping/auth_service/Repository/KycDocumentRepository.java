package com.shipping.auth_service.Repository;

import com.shipping.auth_service.entity.KycDocument;
import com.shipping.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {
    List<KycDocument> findByKyc_Id(Long kycId);
    Optional<KycDocument> findByDocumentNumber(String documentNumber);
}