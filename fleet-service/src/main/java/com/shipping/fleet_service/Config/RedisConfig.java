package com.shipping.fleet_service.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.shipping.fleet_service.DTO")
                .allowIfSubType("java.util")
                .allowIfSubType("java.lang")
                .allowIfSubType("java.time")
                .allowIfSubType("java.lang.")// LocalDateTime fields need this too
                .build();
        ObjectMapper redisObjectMapper = new ObjectMapper();
        redisObjectMapper.registerModule(new JavaTimeModule()).activateDefaultTyping(ptv,ObjectMapper.DefaultTyping.EVERYTHING);
        redisObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // NOTE: no activateDefaultTyping — each cache holds one known concrete DTO type,
        // so polymorphic type wrapping is unnecessary and was the source of the bug.

        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(30))
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(new StringRedisSerializer())
                        )
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(
                                                new GenericJackson2JsonRedisSerializer(redisObjectMapper)
                                        )
                        )
                        .disableCachingNullValues();

        return builder -> builder
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration("ports", defaultConfig)
                .withCacheConfiguration("portName", defaultConfig)
                .withCacheConfiguration("portCode", defaultConfig)
                .withCacheConfiguration("ships", defaultConfig)
                .withCacheConfiguration("vehicles", defaultConfig)
                .withCacheConfiguration("vehicleByNumber", defaultConfig);
    }
}