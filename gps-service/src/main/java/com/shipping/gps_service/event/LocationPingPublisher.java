
package com.shipping.gps_service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocationPingPublisher {

    private static final String TOPIC = "container-location-pings";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CompletableFuture<SendResult<String, Object>> publish(
            LocationPingEvent event
    ) {
        return kafkaTemplate.send(
                TOPIC,
                String.valueOf(event.containerId()),
                event
        ).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error(
                        "Failed to publish location ping for containerId={}",
                        event.containerId(),
                        ex
                );
            } else {
                log.debug(
                        "Published location ping for containerId={}, partition={}, offset={}",
                        event.containerId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                );
            }
        });
    }
}
