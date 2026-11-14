package com.shipping.payment_service.event;

import com.shipping.payment_service.event.outgoing.PaymentFailedEvent;
import com.shipping.payment_service.event.outgoing.PaymentSuccededEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentEventPublisher {
    private static final String SUCCEEDED_TOPIC = "payment-succeeded-events";
    private static final String FAILED_TOPIC = "payment-failed-events";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentSucceeded(PaymentSuccededEvent event) {
        send(SUCCEEDED_TOPIC, event.paymentId(), event, "PaymentSucceededEvent");
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        send(FAILED_TOPIC, event.paymentId(), event, "PaymentFailedEvent");
    }

    private void send(String topic, Long key, Object event, String eventName) {
        kafkaTemplate.send(topic, String.valueOf(key), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish {} for key={}", eventName, key, ex);
                    } else {
                        log.info("{} published. key={}, topic={}, offset={}",
                                eventName, key, result.getRecordMetadata().topic(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
