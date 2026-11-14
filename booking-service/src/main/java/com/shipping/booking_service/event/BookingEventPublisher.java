package com.shipping.booking_service.event;

import com.shipping.booking_service.event.incoming.PortEvent.PortTaskEvent;
import com.shipping.booking_service.event.outgoing.BookingCancelledEvent;
import com.shipping.booking_service.event.outgoing.BookingCreatedEvent;
import com.shipping.booking_service.event.outgoing.BookingStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventPublisher {
    private static final String CREATED_TOPIC = "booking-created-events";
    private static final String CANCELLED_TOPIC = "booking-cancelled-events";
    private static final String STATUS_CHANGED_TOPIC = "booking-status-changed-events";

    private final KafkaTemplate<String,Object> kafkaTemplate;
    public void publishBookingCreated(BookingCreatedEvent event){
        send(CREATED_TOPIC,event.bookingId(),event,"BookingCreatedEvent");
    }
    public void publishBookingCancelled(BookingCancelledEvent event) {
        send(CANCELLED_TOPIC, event.bookingId(), event, "BookingCancelled");
    }
    public void publishBookingStatusChanged(BookingStatusChangedEvent event) {
        send(STATUS_CHANGED_TOPIC, event.bookingId(), event, "BookingStatusChanged");
    }
    private void send(String topic, Long key, Object event, String eventName) {
        kafkaTemplate.send(topic, String.valueOf(key), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish {} for bookingId={}", eventName, key, ex);
                    } else {
                        log.info("{} published. bookingId={}, topic={}, offset={}",
                                eventName, key, result.getRecordMetadata().topic(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
    public void publishPickupOtp(PickupOtpEvent e) { send("pickup-otp-events", e.bookingId(), e, "PickupOtp"); }
    public void publishPortTask(PortTaskEvent e) { send("port-task-events", e.bookingId(), e, "PortTask"); }
}
