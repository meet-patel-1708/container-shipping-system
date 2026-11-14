package com.shipping.auth_service.Config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic userRegisteredTopic() {

        return TopicBuilder
                .name("user-registered-events")
                .partitions(3)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic userUpdatedTopic(){
        return TopicBuilder.name("user-updated-events")
                .partitions(3)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic userDeletedTopic(){
        return TopicBuilder.name("user-deleted-events")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
