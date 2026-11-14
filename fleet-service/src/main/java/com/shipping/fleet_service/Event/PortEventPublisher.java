package com.shipping.fleet_service.Event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PortEventPublisher {
    private static final String CREATED_TOPIC="port-created-events";
    private static final String UPDATED_TOPIC="port-updated-events";
    private static final String DELETED_TOPIC="port-deleted-events";
    private final KafkaTemplate<String,Object> kafkaTemplate;

    public void publishPortCreated(PortCreatedEvent event){
        send(CREATED_TOPIC,event.portId(),event,"PortCreatedEvent");
    }

    public void publishPortUpdated(PortUpdatedEvent event){
        send(UPDATED_TOPIC,event.portId(),event,"PortUpdatedEvent");
    }

    public void publishPortDeleted(PortDeletedEvent event){
        send(DELETED_TOPIC,event.portId(),event,"PortDeletedEvent");
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
