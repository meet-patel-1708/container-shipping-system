package shipping.notification_service.dto.response;

import shipping.notification_service.entity.Channel;
import shipping.notification_service.entity.NotificationStatus;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long userId,
        Long bookingId,
        Channel channel,
        String title,
        String message,
        NotificationStatus status,
        LocalDateTime sentAt,
        LocalDateTime createdAt
) {
}
