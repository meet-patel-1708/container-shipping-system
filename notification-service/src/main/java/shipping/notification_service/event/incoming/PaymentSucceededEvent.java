package shipping.notification_service.event.incoming;

public record PaymentSucceededEvent(
        Long paymentId,
        Long bookingId,
        Long userId,
        String transactionReference
) {
}
