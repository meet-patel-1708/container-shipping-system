package shipping.notification_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shipping.notification_service.entity.UserContact;

public interface UserContactRepository extends JpaRepository<UserContact, Long> {}
