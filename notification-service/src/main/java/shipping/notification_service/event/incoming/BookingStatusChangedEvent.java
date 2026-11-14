package shipping.notification_service.event.incoming;

public record BookingStatusChangedEvent(Long bookingId, Long userId, String oldStatus, String newStatus,String note) {}
