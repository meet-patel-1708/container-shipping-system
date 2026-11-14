package shipping.notification_service.event.incoming;

public record PickupOtpEvent(Long bookingId, Long userId, String otp, int validMinutes,String purpose) {}