package shipping.notification_service.event.consumer;

public record BookingCancelledEvent(Long bookingId, Long userId, Long containerId) {}
