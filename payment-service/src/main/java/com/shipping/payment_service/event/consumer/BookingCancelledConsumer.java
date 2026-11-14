package com.shipping.payment_service.event.consumer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipping.payment_service.event.incoming.BookingCancelledEvent;
import com.shipping.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingCancelledConsumer {

    private final PaymentService service;
    private final ObjectMapper mapper;

    @KafkaListener(topics = "booking-cancelled-events", groupId = "${spring.kafka.consumer.group-id}")
    public void onCancelled(String payload) {
        try {
            BookingCancelledEvent e = mapper.readValue(payload, BookingCancelledEvent.class);
            service.handleBookingCancelled(e.bookingId());
        } catch (Exception ex) {
            log.error("FAILED TO PROCESS booking-cancelled-events payload={}", payload, ex);
        }
    }
}