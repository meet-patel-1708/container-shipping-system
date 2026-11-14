package com.shipping.fleet_service.Event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class ShipEventPublisher {
    private static final String CREATED_TOPIC="ship-created-events";
    private static final String UPDATED_TOPIC="ship-updated-events";
    private static final String DELETED_TOPIC="ship-deleted-events";
    private final KafkaTemplate<String,Object> kafkaTemplate;

    public void publishShipCreated(ShipCreatedEvent event){
        send(CREATED_TOPIC,event.shipId(),event,"ShipCreatedEvent");
    }

    public void publishShipUpdated(ShipUpdatedEvent event){
        send(UPDATED_TOPIC,event.shipId(),event,"ShipUpdatedEvent");
    }

    public void publishShipDeleted(ShipDeletedEvent event){
        send(DELETED_TOPIC,event.shipId(),event,"ShipDeletedEvent");
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
