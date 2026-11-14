package com.shipping.payment_service.event.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipping.payment_service.event.incoming.BookingCreatedEvent;
import com.shipping.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventConsumer {
    private final PaymentService service;
    private final ObjectMapper mapper;
    @KafkaListener(topics = "booking-created-events", groupId = "${spring.kafka.consumer.group-id}")
    public void onBookingCreated(String payload) {
        try {
            BookingCreatedEvent event = mapper.readValue(payload, BookingCreatedEvent.class);
            service.createPendingPayment(event.bookingId(),event.userId(), event.totalAmount());
            log.info("Pending payment created for bookingId={}", event.bookingId());
        } catch (Exception e) {
            log.error("!!!!! FAILED TO PROCESS booking-created-events payload={} !!!!!", payload, e);
        }
    }
}
