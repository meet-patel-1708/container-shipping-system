package com.shipping.booking_service.event.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipping.booking_service.event.incoming.VehicleEvent.VehicleCreatedEvent;
import com.shipping.booking_service.event.incoming.VehicleEvent.VehicleUpdatedEvent;
import com.shipping.booking_service.event.incoming.VehicleEvent.VehicleDeletedEvent;
import com.shipping.booking_service.entity.Replica.VehicleReplica;
import com.shipping.booking_service.repository.Replica.VehicleReplicaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class VehicleEventConsumer {

    private final VehicleReplicaRepository repository;
    private final ObjectMapper mapper;

    @KafkaListener(topics = "vehicle-created-events", groupId = "${spring.kafka.consumer.group-id}")
    public void onVehicleCreated(String payload) {
        try {
            VehicleCreatedEvent event = mapper.readValue(payload, VehicleCreatedEvent.class);
            VehicleReplica replica = new VehicleReplica(
                    event.vehicleId(), event.vehicleName(), event.vehicleNumber(), event.vehicleType(), LocalDateTime.now()
            );
            repository.save(replica);
            log.info("VehicleReplica upserted from creation. vehicleId={}", event.vehicleId());
        } catch (Exception e) {
            log.error("FAILED TO PROCESS vehicle-created-events. payload={}", payload, e);
        }
    }

    @KafkaListener(topics = "vehicle-updated-events", groupId = "${spring.kafka.consumer.group-id}")
    public void onVehicleUpdated(String payload) {
        try {
            VehicleUpdatedEvent event = mapper.readValue(payload, VehicleUpdatedEvent.class);
            VehicleReplica replica = repository.findById(event.vehicleId()).orElse(new VehicleReplica());
            replica.setVehicleId(event.vehicleId());
            replica.setVehicleName(event.vehicleName());
            replica.setVehicleNumber(event.vehicleNumber());
            replica.setVehicleType(event.vehicleType());
            replica.setUpdatedAt(LocalDateTime.now());
            repository.save(replica);
            log.info("VehicleReplica updated. vehicleId={}", event.vehicleId());
        } catch (Exception e) {
            log.error("FAILED TO PROCESS vehicle-updated-events. payload={}", payload, e);
        }
    }

    @KafkaListener(topics = "vehicle-deleted-events", groupId = "${spring.kafka.consumer.group-id}")
    public void onVehicleDeleted(String payload) {
        try {
            VehicleDeletedEvent event = mapper.readValue(payload, VehicleDeletedEvent.class);
            repository.deleteById(event.vehicleId());
            log.info("VehicleReplica deleted. vehicleId={}", event.vehicleId());
        } catch (Exception e) {
            log.error("FAILED TO PROCESS vehicle-deleted-events. payload={}", payload, e);
        }
    }
}