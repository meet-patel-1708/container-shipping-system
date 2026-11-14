package com.shipping.booking_service.event.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipping.booking_service.event.incoming.ShipEvent.ShipCreatedEvent;
import com.shipping.booking_service.event.incoming.ShipEvent.ShipUpdatedEvent;
import com.shipping.booking_service.event.incoming.ShipEvent.ShipDeletedEvent;
import com.shipping.booking_service.entity.Replica.ShipReplica;
import com.shipping.booking_service.repository.Replica.ShipReplicaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipEventConsumer {

    private final ShipReplicaRepository repository;
    private final ObjectMapper mapper;

    @KafkaListener(topics = "ship-created-events", groupId = "${spring.kafka.consumer.group-id}")
    public void onShipCreated(String payload) {
        try {
            ShipCreatedEvent event = mapper.readValue(payload, ShipCreatedEvent.class);
            ShipReplica replica = new ShipReplica(
                    event.shipId(), event.shipCompanyName(), event.shipCode(), event.portId(), LocalDateTime.now()
            );
            repository.save(replica);
            log.info("ShipReplica upserted from creation. shipId={}", event.shipId());
        } catch (Exception e) {
            log.error("FAILED TO PROCESS ship-created-events. payload={}", payload, e);
        }
    }

    @KafkaListener(topics = "ship-updated-events", groupId = "${spring.kafka.consumer.group-id}")
    public void onShipUpdated(String payload) {
        try {
            ShipUpdatedEvent event = mapper.readValue(payload, ShipUpdatedEvent.class);
            ShipReplica replica = repository.findById(event.shipId()).orElse(new ShipReplica());
            replica.setShipId(event.shipId());
            replica.setShipCompanyName(event.shipCompanyName());
            replica.setShipCode(event.shipCode());
            replica.setPortId(event.portId());
            replica.setUpdatedAt(LocalDateTime.now());
            repository.save(replica);
            log.info("ShipReplica updated. shipId={}", event.shipId());
        } catch (Exception e) {
            log.error("FAILED TO PROCESS ship-updated-events. payload={}", payload, e);
        }
    }

    @KafkaListener(topics = "ship-deleted-events", groupId = "${spring.kafka.consumer.group-id}")
    public void onShipDeleted(String payload) {
        try {
            ShipDeletedEvent event = mapper.readValue(payload, ShipDeletedEvent.class);
            repository.deleteById(event.shipId());
            log.info("ShipReplica deleted. shipId={}", event.shipId());
        } catch (Exception e) {
            log.error("FAILED TO PROCESS ship-deleted-events. payload={}", payload, e);
        }
    }
}