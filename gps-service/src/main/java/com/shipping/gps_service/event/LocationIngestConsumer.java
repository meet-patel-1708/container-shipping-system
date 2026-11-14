
package com.shipping.gps_service.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipping.gps_service.dto.response.LocationResponse;
import com.shipping.gps_service.entity.ContainerLocation;
import com.shipping.gps_service.mapper.LocationMapper;
import com.shipping.gps_service.repository.ContainerLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocationIngestConsumer {

    private static final String REDIS_KEY_PREFIX = "location:latest:";

    private final ContainerLocationRepository repository;
    private final LocationMapper mapper;
    private final StringRedisTemplate redis;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "container-location-pings",
            groupId = "${spring.kafka.consumer.group-id:gps-service-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void onLocationPing(String payload) {
        log.info("Received location ping from Kafka");

        try {
            LocationPingEvent event =
                    objectMapper.readValue(payload, LocationPingEvent.class);

            if (event.containerId() == null
                    || event.latitude() == null
                    || event.longitude() == null) {
                throw new IllegalArgumentException(
                        "containerId, latitude and longitude are required"
                );
            }

            ContainerLocation location = new ContainerLocation();
            location.setContainerId(event.containerId());
            location.setLatitude(event.latitude());
            location.setLongitude(event.longitude());
            location.setSpeedKmph(event.speedKmph());
            location.setHeading(event.heading());
            location.setRecordedAt(OffsetDateTime.now());

            // The Kafka consumer is the only component saving this ping.
            ContainerLocation saved = repository.save(location);

            LocationResponse response = mapper.toResponse(saved);

            // Update Redis with the latest location.
            String redisKey = REDIS_KEY_PREFIX + event.containerId();
            String responseJson = objectMapper.writeValueAsString(response);
            redis.opsForValue().set(redisKey, responseJson);

            // Notify connected WebSocket clients.
            messagingTemplate.convertAndSend(
                    "/topic/container/" + event.containerId(),
                    response
            );

            log.info(
                    "Processed location ping for containerId={}",
                    event.containerId()
            );

        } catch (Exception e) {
            log.error("Failed to process location ping", e);

            // Do not swallow the exception. Spring Kafka's error handler
            // needs it to trigger retries and dead-letter publishing.
            throw new IllegalStateException(
                    "Location ping processing failed",
                    e
            );
        }
    }
}
