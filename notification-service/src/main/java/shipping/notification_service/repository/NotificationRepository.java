package shipping.notification_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import shipping.notification_service.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Long> {
    Page<Notification> findByUserId(Long userId, Pageable pageable);
    Page<Notification> findByBookingId(Long bookingId, Pageable pageable);
}
