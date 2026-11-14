package shipping.notification_service.event.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import shipping.notification_service.service.NotificationService;
import shipping.notification_service.event.incoming.BookingStatusChangedEvent;
import shipping.notification_service.event.incoming.PickupOtpEvent;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingMovementConsumer {
    private static final Map<String, String[]> TEXT = Map.of(
            "DELIVERED", new String[]{"Delivered", "Booking #%d has been delivered."},
            "REJECTED", new String[]{"Goods rejected at the port", "Your goods for booking #%d were rejected at the port."},
            "CANCELLED", new String[]{"Booking cancelled", "Booking #%d was cancelled."});


    private final NotificationService service;
    private final ObjectMapper mapper;

    @KafkaListener(topics = "booking-status-changed-events", groupId = "${spring.kafka.consumer.group-id}")
    public void onStatus(String payload) {
        try {
            BookingStatusChangedEvent e = mapper.readValue(payload, BookingStatusChangedEvent.class);
            String[] t = TEXT.get(e.newStatus());
            if (t == null) return;
            String msg = t[1].formatted(e.bookingId());
            if (e.note() != null && !e.note().isBlank()) msg += " Reason: " + e.note();
            service.createAndSend(e.userId(), e.bookingId(), t[0], msg);
        } catch (Exception ex) { log.error("FAILED status event {}", payload, ex); }
    }

    @KafkaListener(topics = "pickup-otp-events", groupId = "${spring.kafka.consumer.group-id}")
    public void onOtp(String payload) {
        try {
            PickupOtpEvent e = mapper.readValue(payload, PickupOtpEvent.class);
            service.sendOtp(e.userId(), e.bookingId(), e.otp(), e.validMinutes(),e.purpose());
        } catch (Exception ex) { log.error("FAILED otp event", ex); }   // never log the payload: it holds the code
    }
}