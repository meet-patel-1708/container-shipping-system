package com.shipping.catalog_service.Event;

import lombok.RequiredArgsConstructor;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class ProductEventPublisher {

    private static final String CREATED_TOPIC = "product-created-events";
    private static final String UPDATED_TOPIC = "product-updated-events";
    private static final String DELETED_TOPIC = "product-deleted-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishProductCreated(ProductCreatedEvent event) {
        send(CREATED_TOPIC, event.productId(), event, "ProductCreatedEvent");
    }

    public void publishProductUpdated(ProductUpdatedEvent event) {
        send(UPDATED_TOPIC, event.productId(), event, "ProductUpdatedEvent");
    }

    public void publishProductDeleted(ProductDeletedEvent event) {
        send(DELETED_TOPIC, event.productId(), event, "ProductDeletedEvent");
    }

    private void send(String topic, Long key, Object event, String eventName) {
        kafkaTemplate.send(topic, String.valueOf(key), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        //log.error("{} failed. key={}, topic={}", eventName, key, topic, ex);
                    } else {
                        //log.info("{} published. key={}, topic={}, offset={}",eventName, key, topic, result.getRecordMetadata().offset());
                    }
                });
    }
}