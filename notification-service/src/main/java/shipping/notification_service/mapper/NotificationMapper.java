package shipping.notification_service.mapper;

import org.springframework.stereotype.Component;
import shipping.notification_service.dto.response.NotificationResponse;
import shipping.notification_service.entity.Notification;

@Component
public class NotificationMapper {
    public NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getUserId(), n.getBookingId(), n.getChannel(),
                n.getTitle(), n.getMessage(), n.getStatus(), n.getSentAt(), n.getCreatedAt()
        );
    }
}
