package com.shipping.booking_service.event.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipping.booking_service.event.incoming.ProductEvent.ProductCreatedEvent;
import com.shipping.booking_service.event.incoming.ProductEvent.ProductUpdatedEvent;
import com.shipping.booking_service.event.incoming.ProductEvent.ProductDeletedEvent;
import com.shipping.booking_service.entity.Replica.ProductReplica;
import com.shipping.booking_service.repository.Replica.ProductReplicaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventConsumer {

    private final ProductReplicaRepository repository;
    private final ObjectMapper mapper;

    @KafkaListener(topics = "product-created-events", groupId = "${spring.kafka.consumer.group-id}")
    public void onProductCreated(String payload) {
        try {
        ProductCreatedEvent event = mapper.readValue(payload, ProductCreatedEvent.class);
        ProductReplica replica = new ProductReplica(
                event.productId(), event.productCode(), event.productName(),
                event.productPrice(), event.productWeight(), LocalDateTime.now()
        );
        repository.save(replica);
        log.info("ProductReplica upserted from creation. productId={}", event.productId());
        } catch (Exception e) {
            log.error("FAILED TO PROCESS port-created-events. payload={}", payload, e);
        }
    }

    @KafkaListener(topics = "product-updated-events", groupId = "${spring.kafka.consumer.group-id}")
    public void onProductUpdated(String payload) {
        try{ ProductUpdatedEvent event = mapper.readValue(payload, ProductUpdatedEvent.class);
        ProductReplica replica = repository.findById(event.productId()).orElse(new ProductReplica());
        replica.setProductId(event.productId());
        replica.setProductCode(event.productCode());
        replica.setProductName(event.productName());
        replica.setProductPrice(event.productPrice());
        replica.setProductWeight(event.productWeight());
        replica.setUpdatedAt(LocalDateTime.now());
        repository.save(replica);
        log.info("ProductReplica updated. productId={}", event.productId());
    } catch (Exception e) {
        log.error("FAILED TO PROCESS port-created-events. payload={}", payload, e);
    }
    }

    @KafkaListener(topics = "product-deleted-events", groupId = "${spring.kafka.consumer.group-id}")
    public void onProductDeleted(String payload) {
    try{
        ProductDeletedEvent event = mapper.readValue(payload, ProductDeletedEvent.class);
        repository.deleteById(event.productId());
        log.info("ProductReplica deleted. productId={}", event.productId());
    } catch (Exception e) {
        log.error("FAILED TO PROCESS port-created-events. payload={}", payload, e);
        }
    }
}
