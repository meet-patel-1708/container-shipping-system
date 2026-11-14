package com.shipping.booking_service.event.Consumer;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipping.booking_service.service.MovementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaymentCompletedEvent(Long bookingId) {}

    private final MovementService movement;
    private final ObjectMapper mapper;

    @KafkaListener(topics = "payment-succeeded-events", groupId = "${spring.kafka.consumer.group-id}")
    public void onPaid(String payload) {
        try {
            movement.markPaid(mapper.readValue(payload, PaymentCompletedEvent.class).bookingId());
        } catch (Exception e) {
            log.error("FAILED TO PROCESS payment-succeeded-events. payload={}", payload, e);
        }
    }
}