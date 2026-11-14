package com.shipping.fleet_service.Service.IMPL;

import com.shipping.fleet_service.DTO.Request.PortRequest;
import com.shipping.fleet_service.DTO.Response.PagedResponse;
import com.shipping.fleet_service.DTO.Response.PortResponse;
import com.shipping.fleet_service.Entity.Port;
import com.shipping.fleet_service.Event.PortCreatedEvent;
import com.shipping.fleet_service.Event.PortDeletedEvent;
import com.shipping.fleet_service.Event.PortEventPublisher;
import com.shipping.fleet_service.Event.PortUpdatedEvent;
import com.shipping.fleet_service.Exception.PortCodeAlreadyExistException;
import com.shipping.fleet_service.Exception.PortNotFoundException;
import com.shipping.fleet_service.Mapper.PortMapper;
import com.shipping.fleet_service.Repository.PortRepository;
import com.shipping.fleet_service.Service.PortService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PortServiceImpl implements PortService {

    private final PortRepository repository;
    private final PortMapper mapper;
    private final PortEventPublisher publisher;
    @Override
    @Transactional

    public PortResponse create(PortRequest request) {
        if(repository.existsByPortCode(request.portCode())){
            throw new PortCodeAlreadyExistException(request.portCode());
        }
        Port port = mapper.toEntity(request);
        Port saved = repository.save(port);
        publisher.publishPortCreated(new PortCreatedEvent(saved.getId(),saved.getPortName(),saved.getPortCode()));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    @Cacheable(value = "ports", key = "#id")
    public PortResponse getById(Long id) {
        Port port = repository.findById(id).orElseThrow(()->new PortNotFoundException(id));
        return mapper.toResponse(port);
    }

    @Override
    @Transactional
    @Cacheable(value = "portCode",key = "#portCode")
    public PortResponse getByCode(String portCode) {
        Port port = repository.findByPortCode(portCode).orElseThrow(()->new PortNotFoundException(portCode));
        return mapper.toResponse(port);
    }

    @Override
    @Transactional
    public PagedResponse<PortResponse> list(Pageable pageable) {
        Page<PortResponse> page = repository.findAll(pageable).map(mapper::toResponse);
        return PagedResponse.from(page);
    }

    @Override
    @Transactional
    @Cacheable(value = "portName",key = "#portName")
    public PortResponse getPortByName(String portName) {
        Port port =repository.findByPortName(portName).orElseThrow(()->new PortNotFoundException(portName));
        return mapper.toResponse(port);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"ports", "portCode", "portName", "ships"}, allEntries = true)
    public PortResponse update(String PortCode, PortRequest request) {
        Port port = repository.findByPortCode(PortCode).orElseThrow(()->new PortNotFoundException(PortCode));
        if(!port.getPortCode().equals(request.portCode()) && repository.existsByPortCode(request.portCode())){
            throw new PortCodeAlreadyExistException(request.portCode());
        }
        mapper.toUpdate(request,port);
        Port saved = repository.save(port);
        publisher.publishPortUpdated(new PortUpdatedEvent(saved.getId(),saved.getPortName(),saved.getPortCode()));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"ports", "portCode", "portName", "ships"}, allEntries = true)
    public void delete(String portCode) {
        Port port = repository.findByPortCode(portCode).orElseThrow(()->new PortNotFoundException(portCode));
        repository.delete(port);
        publisher.publishPortDeleted(new PortDeletedEvent(port.getId()));
    }
}
