package com.shipping.auth_service.Repository;

import com.shipping.auth_service.entity.StaffInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffInvitationRepository extends JpaRepository<StaffInvitation,Long> {
    Optional<StaffInvitation> findByUser_UserId(Long userId);
    Optional<StaffInvitation> findByTokenHash(String tokenHash);
}
