package com.shipping.booking_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic bookingCreatedTopic(){
        return TopicBuilder.name("booking-created-events").partitions(3).replicas(1).build();
    }
    @Bean
    public NewTopic bookingCancelledTopic(){
        return TopicBuilder.name("booking-cancelled-events").partitions(3).replicas(1).build();
    }
    @Bean
    public NewTopic bookingStatusChangedTopic(){
        return TopicBuilder.name("booking-status-changed-events").partitions(3).replicas(1).build();
    }
    @Bean
        public NewTopic pickedOtpEvent(){
        return TopicBuilder.name("pickup-otp-events").partitions(3).replicas(1).build();
    }
    @Bean
    public NewTopic portTaskEvent(){
        return TopicBuilder.name("port-task-events").partitions(3).replicas(1).build();
    }
}
