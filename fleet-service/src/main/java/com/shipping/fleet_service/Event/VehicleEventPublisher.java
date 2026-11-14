package com.shipping.fleet_service.Event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VehicleEventPublisher {
    private static final String CREATED_TOPIC="vehicle-created-events";
    private static final String UPDATED_TOPIC="vehicle-updated-events";
    private static final String DELETED_TOPIC="vehicle-deleted-events";
    private final KafkaTemplate<String,Object> kafkaTemplate;

    public void publishVehicleCreated(VehicleCreatedEvent event){
        send(CREATED_TOPIC,event.vehicleId(),event,"ShipCreatedEvent");
    }

    public void publishVehicleUpdated(VehicleUpdatedEvent event){
        send(UPDATED_TOPIC,event.vehicleId(),event,"ShipUpdatedEvent");
    }

    public void publishVehicleDeleted(VehicleDeletedEvent event){
        send(DELETED_TOPIC,event.vehicleId(),event,"ShipDeletedEvent");
    }

    private void send(String topic,Long key,Object Event,String eventName){
        kafkaTemplate.send(topic,String.valueOf(key),Event).whenComplete((result,ex)->{
            if(ex!=null){
                log.error("Failed to publish {} for key={}",eventName,key);
            }
            else{
                log.info("Successfully to publish {} for key={}",eventName,key);
            }
        });
    }


}
