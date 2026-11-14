package com.shipping.booking_service.event.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipping.booking_service.entity.Replica.PortReplica;
import com.shipping.booking_service.event.incoming.PortEvent.PortCreatedEvent;
import com.shipping.booking_service.event.incoming.PortEvent.PortUpdatedEvent;
import com.shipping.booking_service.event.incoming.PortEvent.PortDeletedEvent;
import com.shipping.booking_service.repository.Replica.PortReplicaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortEventConsumer {

    private final PortReplicaRepository repository;
    private final ObjectMapper mapper;

    @KafkaListener(topics = "port-created-events", groupId = "${spring.kafka.consumer.group-id}")
    public void onPortCreated(String payload) {
        try {
            PortCreatedEvent event = mapper.readValue(payload, PortCreatedEvent.class);
            PortReplica replica = new PortReplica(
                    event.portId(), event.portName(), event.portCode(), LocalDateTime.now()
            );
            repository.save(replica);
            log.info("PortReplica upserted from creation. portId={}", event.portId());
        } catch (Exception e) {
            log.error("FAILED TO PROCESS port-created-events. payload={}", payload, e);
        }
    }

    @KafkaListener(topics = "port-updated-events", groupId = "${spring.kafka.consumer.group-id}")
    public void onPortUpdated(String payload) {
        try {
            PortUpdatedEvent event = mapper.readValue(payload, PortUpdatedEvent.class);
            PortReplica replica = repository.findById(event.portId()).orElse(new PortReplica());
            replica.setPortId(event.portId());
            replica.setPortName(event.portName());
            replica.setPortCode(event.portCode());
            replica.setUpdatedAt(LocalDateTime.now());
            repository.save(replica);
            log.info("PortReplica updated. portId={}", event.portId());
        } catch (Exception e) {
            log.error("FAILED TO PROCESS port-updated-events. payload={}", payload, e);
        }
    }

    @KafkaListener(topics = "port-deleted-events", groupId = "${spring.kafka.consumer.group-id}")
    public void onPortDeleted(String payload) {
        try {
            PortDeletedEvent event = mapper.readValue(payload, PortDeletedEvent.class);
            repository.deleteById(event.portId());
            log.info("PortReplica deleted. portId={}", event.portId());
        } catch (Exception e) {
            log.error("FAILED TO PROCESS port-deleted-events. payload={}", payload, e);
        }
    }
}