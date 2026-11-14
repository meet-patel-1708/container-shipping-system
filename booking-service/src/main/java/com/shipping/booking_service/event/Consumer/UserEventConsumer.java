package com.shipping.booking_service.event.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipping.booking_service.entity.Replica.UserReplica;
import com.shipping.booking_service.event.incoming.UserEvent.UserDeleteEvent;
import com.shipping.booking_service.event.incoming.UserEvent.UserRegisterEvent;
import com.shipping.booking_service.repository.Replica.UserReplicaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {

    private final UserReplicaRepository repository;
    private final ObjectMapper mapper;

    @KafkaListener(
            topics = "user-registered-events",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onUserRegistered(String payload) {

        try {

            log.info("Received user registered payload: {}", payload);
            log.info("Payload type: {}", payload.getClass().getName());

            UserRegisterEvent event =
                    mapper.readValue(payload, UserRegisterEvent.class);

            UserReplica replica = new UserReplica(
                    event.userId(),
                    event.name(),
                    event.email(),
                    null,
                    LocalDateTime.now()
            );

            repository.save(replica);

            log.info(
                    "UserReplica saved successfully. userId={}",
                    event.userId()
            );

        } catch (Exception e) {

            log.error(
                    "FAILED TO PROCESS user-registered-events. payload={}",
                    payload,
                    e
            );
        }
    }
}