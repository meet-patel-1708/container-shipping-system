package com.shipping.fleet_service.Config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic portCreatedTopic(){
        return TopicBuilder.name("port-created-events").partitions(3).replicas(1).build();
    }
    @Bean
    public NewTopic portUpdatedTopic(){
        return TopicBuilder.name("port-updated-events").partitions(3).replicas(1).build();
    }
    @Bean
    public NewTopic portDeletedTopic(){
        return TopicBuilder.name("port-deleted-events").partitions(3).replicas(1).build();
    }
    @Bean
    public NewTopic shipCreatedTopic(){
        return TopicBuilder.name("ship-created-events").partitions(3).replicas(1).build();
    }
    @Bean
    public NewTopic shipUpdatedTopic(){
        return TopicBuilder.name("ship-updated-events").partitions(3).replicas(1).build();
    }
    @Bean
    public NewTopic shipDeletedTopic(){
        return TopicBuilder.name("ship-deleted-events").partitions(3).replicas(1).build();
    }
    @Bean
    public NewTopic vehicleCreatedTopic(){
        return TopicBuilder.name("vehicle-created-events").partitions(3).replicas(1).build();
    }
    @Bean
    public NewTopic vehicleUpdatedTopic(){
        return TopicBuilder.name("vehicle-updated-events").partitions(3).replicas(1).build();
    }
    @Bean
    public NewTopic vehicleDeletedTopic(){
        return TopicBuilder.name("vehicle-deleted-events").partitions(3).replicas(1).build();
    }
}
