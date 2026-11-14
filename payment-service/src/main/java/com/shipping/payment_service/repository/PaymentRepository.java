package com.shipping.payment_service.repository;

import com.shipping.payment_service.entity.Payment;
import com.shipping.payment_service.entity.PaymentStatus;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByBookingId(Long bookingId);
    Optional<Payment> findByIdAndUserId(Long id, Long userId);
    Optional<Payment> findByBookingIdAndUserId(Long bookingId, Long userId);
    Optional<Payment> findByBookingId(Long bookingId);
    Page<Payment> findByUserId(Long userId, Pageable pageable);
    Page<Payment> findByUserIdAndPaymentStatus(Long userId, PaymentStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)   // stops a double-click paying twice
    @Query("select p from Payment p where p.id = :id and p.userId = :userId")
    Optional<Payment> findForUpdate(@Param("id") Long id, @Param("userId") Long userId);
    Page<Payment> findByPaymentStatus(PaymentStatus status, Pageable pageable);
}
