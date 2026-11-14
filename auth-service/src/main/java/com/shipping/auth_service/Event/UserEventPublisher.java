package com.shipping.auth_service.Event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private static final String TOPIC = "user-registered-events";
    private static final String UPDATED_TOPIC = "user-updated-events";
    private static final String DELETED_TOPIC = "user-deleted-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserRegistered(UserRegisterEvent event) {

        kafkaTemplate
                .send(
                        TOPIC,
                        String.valueOf(event.userId()),
                        event
                )
                .whenComplete((result, ex) -> {

                    if (ex != null) {
                        log.error(
                                "Failed to publish UserRegisteredEvent for userId={}",
                                event.userId(),
                                ex
                        );
                    } else {
                        log.info(
                                "UserRegisteredEvent published successfully. userId={}, topic={}, partition={}, offset={}",
                                event.userId(),
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        );
                    }
                });
    }
    public void publishUserUpdated(UserUpdatedEvent event){
        kafkaTemplate.send(UPDATED_TOPIC,String.valueOf(event.userId()),event)
                .whenComplete((result,ex)->{
                    if(ex!=null){
                        log.error("Failed to publish user updated event with user id = {}"+event.userId(),ex);
                    }
                    else{
                        log.info("UserUpdatedEvent published. userId={}, offset={}",
                                event.userId(), result.getRecordMetadata().offset());
                    }
                });
    }
    public void publishUserDeleted(UserDeletedEvent event){
        kafkaTemplate.send(DELETED_TOPIC,String.valueOf(event.userId()),event)
                .whenComplete((result,ex)->{
                    if(ex!=null){
                        log.error("Failed to publish deleted event with user id = {}"+event.userId(),ex);
                    }
                    else{
                        log.info("UserUpdatedEvenet published. userid = {},offset={}",event.userId(),result.getRecordMetadata().offset());
                    }
                });
    }
}