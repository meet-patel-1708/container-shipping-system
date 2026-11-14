package com.shipping.gps_service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipping.gps_service.dto.response.LocationResponse;
import com.shipping.gps_service.dto.response.PagedResponse;
import com.shipping.gps_service.entity.ContainerLocation;
import com.shipping.gps_service.exception.LocationNotFoundException;
import com.shipping.gps_service.mapper.LocationMapper;
import com.shipping.gps_service.repository.ContainerLocationRepository;
import com.shipping.gps_service.service.ContainerLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContainerLocationServiceImpl implements ContainerLocationService {

    private static final String REDIS_KEY_PREFIX = "location:latest:";

    private final ContainerLocationRepository repository;
    private final LocationMapper mapper;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    @Override
    public LocationResponse getLatest(Long containerId) {
        String key = REDIS_KEY_PREFIX + containerId;

        String cached = redis.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, LocationResponse.class);
            } catch (JsonProcessingException e) {
                log.warn("Unreadable cache entry for containerId={}, falling back to Postgres", containerId, e);
            }
        }

        log.warn("Redis cache miss for containerId={}, falling back to Postgres", containerId);
        ContainerLocation location = repository.findLatestByContainerId(containerId)
                .orElseThrow(() -> new LocationNotFoundException(containerId));
        LocationResponse response = mapper.toResponse(location);
        try {
            redis.opsForValue().set(key, objectMapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
            log.warn("Could not repopulate cache for containerId={}", containerId, e);
        }
        return response;
    }

    @Override
    public PagedResponse<LocationResponse> getHistory(Long containerId, Pageable pageable) {
        Page<LocationResponse> page = repository.findByContainerIdOrderByRecordedAtDesc(containerId, pageable)
                .map(mapper::toResponse);
        return PagedResponse.from(page);
    }
}