package com.shipping.catalog_service.Config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic productCreatedTopic(){
        return TopicBuilder.name("product-created-events").partitions(3).replicas(1).build();
    }
    @Bean
    public NewTopic productUpdatedTopic(){
        return TopicBuilder.name("product-updated-events").partitions(3).replicas(1).build();
    }
    @Bean
    public NewTopic productDeletedTopic(){
        return TopicBuilder.name("product-deleted-events").partitions(3).replicas(1).build();
    }
}
