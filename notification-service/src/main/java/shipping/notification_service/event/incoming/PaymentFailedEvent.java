package shipping.notification_service.event.incoming;

public record PaymentFailedEvent(
        Long paymentId,
        Long bookingId,
        Long userId,
        String transactionReference
) {
}
