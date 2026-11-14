package com.shipping.booking_service.service.impl;

import com.shipping.booking_service.dto.Request.ContainerRequest;
import com.shipping.booking_service.dto.Response.ContainerResponse;
import com.shipping.booking_service.dto.Response.PagedResponse;
import com.shipping.booking_service.exception.ContainerNotFoundException;
import com.shipping.booking_service.mapper.ContainerMapper;
import com.shipping.booking_service.service.ContainerService;
import com.shipping.booking_service.entity.Container;
import com.shipping.booking_service.entity.Status;
import com.shipping.booking_service.repository.ContainerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContainerServiceImpl implements ContainerService {

    private final ContainerRepository repository;
    private final ContainerMapper mapper;

    @Override
    @Transactional
    public ContainerResponse create(ContainerRequest request) {
        Container container = mapper.toEntity(request);
        Container saved = repository.save(container);
        return mapper.toResponse(saved);
    }

    @Override
    @Cacheable(value = "containers", key = "#containerId")
    public ContainerResponse getById(Long containerId) {
        Container container = repository.findById(containerId)
                .orElseThrow(() -> new ContainerNotFoundException(containerId));
        return mapper.toResponse(container);
    }

    @Override
    @Cacheable(value = "containerLists", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public PagedResponse<ContainerResponse> list(Pageable pageable) {
        Page<ContainerResponse> page = repository.findAll(pageable).map(mapper::toResponse);
        return PagedResponse.from(page);
    }

    @Override
    @Cacheable(value = "containerLists", key = "#status + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public PagedResponse<ContainerResponse> listByStatus(Status status, Pageable pageable) {
        Page<ContainerResponse> page = repository.findByStatus(status, pageable).map(mapper::toResponse);
        return PagedResponse.from(page);
    }
    @Override
    @Caching(evict = {
            @CacheEvict(value = "containers", key = "#containerId"),
            @CacheEvict(value = "containerLists", allEntries = true)
    })
    public void evictCache(Long containerId) { }
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "containers", key = "#containerId"),
            @CacheEvict(value = "containerLists", allEntries = true)
    })
    public ContainerResponse updateStatus(Long containerId, Status status) {
        Container c = repository.findByIdForUpdate(containerId)
                .orElseThrow(() -> new ContainerNotFoundException(containerId));
        if (c.getStatus() == Status.IN_TRANSIT || status == Status.IN_TRANSIT) {
            throw new IllegalStateException("IN_TRANSIT is managed by bookings");
        }
        c.setStatus(status);
        return mapper.toResponse(repository.save(c));
    }
}