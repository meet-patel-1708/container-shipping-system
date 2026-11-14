package com.shipping.fleet_service.Service.IMPL;

import com.shipping.fleet_service.DTO.Request.ShipRequest;
import com.shipping.fleet_service.DTO.Response.PagedResponse;
import com.shipping.fleet_service.DTO.Response.ShipResponse;
import com.shipping.fleet_service.Entity.Port;
import com.shipping.fleet_service.Entity.Ship;
import com.shipping.fleet_service.Event.ShipCreatedEvent;
import com.shipping.fleet_service.Event.ShipDeletedEvent;
import com.shipping.fleet_service.Event.ShipEventPublisher;
import com.shipping.fleet_service.Event.ShipUpdatedEvent;
import com.shipping.fleet_service.Exception.PortNotFoundException;
import com.shipping.fleet_service.Exception.ShipCodeAlreadyExistsException;
import com.shipping.fleet_service.Exception.ShipNotFoundException;
import com.shipping.fleet_service.Mapper.ShipMapper;
import com.shipping.fleet_service.Repository.PortRepository;
import com.shipping.fleet_service.Repository.ShipRepository;
import com.shipping.fleet_service.Service.ShipService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShipServiceImpl implements ShipService {

    private final ShipRepository repository;
    private final PortRepository portRepository;
    private final ShipMapper mapper;
    private final ShipEventPublisher publisher;

    // =========================
    // CREATE
    // =========================

    @Override
    @Transactional
    public ShipResponse create(ShipRequest request) {

        // Check duplicate ship code
        if (repository.existsByShipCode(request.shipCode())) {
            throw new ShipCodeAlreadyExistsException(
                    request.shipCode()
            );
        }

        // Find existing Port using portCode
        Port port = portRepository.findByPortCode(request.portCode())
                .orElseThrow(() ->
                        new PortNotFoundException(
                                request.portCode()
                        )
                );

        // Create Ship and attach Port
        Ship ship = mapper.toEntity(request, port);

        Ship saved = repository.save(ship);
        publisher.publishShipCreated(new ShipCreatedEvent(saved.getId(),saved.getShipCode(),saved.getShipCompanyName(),saved.getPort().getId()));
        return mapper.toResponse(saved);
    }


    // =========================
    // GET BY SHIP CODE
    // =========================

    @Override
    @Transactional
    @Cacheable(
            value = "ships",
            key = "#shipCode"
    )
    public ShipResponse getByCode(String shipCode) {

        Ship ship = repository.findByShipCode(shipCode)
                .orElseThrow(() ->
                        new ShipNotFoundException(shipCode)
                );

        return mapper.toResponse(ship);
    }


    // =========================
    // LIST
    // =========================

    @Override
    @Transactional
    public PagedResponse<ShipResponse> list(Pageable pageable) {

        Page<ShipResponse> page =
                repository.findAll(pageable)
                        .map(mapper::toResponse);

        return PagedResponse.from(page);
    }


    // =========================
    // SEARCH BY COMPANY
    // =========================

    @Override
    @Transactional
    public PagedResponse<ShipResponse> searchByCompany(
            String companyName,
            Pageable pageable
    ) {

        Page<ShipResponse> page =
                repository
                        .findByShipCompanyNameContainingIgnoreCase(
                                companyName,
                                pageable
                        )
                        .map(mapper::toResponse);

        return PagedResponse.from(page);
    }


    // =========================
    // UPDATE BY SHIP CODE
    // =========================

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "vehicles", allEntries = true),
            @CacheEvict(value = "vehicleByNumber", allEntries = true)
    })

    public ShipResponse update(
            String shipCode,
            ShipRequest request
    ) {

        // Find existing ship using business identifier
        Ship ship = repository.findByShipCode(shipCode)
                .orElseThrow(() ->
                        new ShipNotFoundException(shipCode)
                );


        // If user is changing shipCode,
        // check whether new code already exists.
        if (!ship.getShipCode().equals(request.shipCode())
                && repository.existsByShipCode(request.shipCode())) {

            throw new ShipCodeAlreadyExistsException(
                    request.shipCode()
            );
        }


        // Find Port using portCode
        Port port = portRepository.findByPortCode(request.portCode())
                .orElseThrow(() ->
                        new PortNotFoundException(
                                request.portCode()
                        )
                );


        // Update ship fields + relationship
        mapper.toUpdate(
                request,
                ship,
                port
        );


        Ship saved = repository.save(ship);
        publisher.publishShipUpdated(new ShipUpdatedEvent(saved.getId(),saved.getShipCode(),saved.getShipCompanyName(),saved.getPort().getId()));
        return mapper.toResponse(saved);
    }


    // =========================
    // DELETE BY SHIP CODE
    // =========================

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "vehicles", allEntries = true),
            @CacheEvict(value = "vehicleByNumber", allEntries = true)
    })

    public void delete(String shipCode) {

        Ship ship = repository.findByShipCode(shipCode)
                .orElseThrow(() ->
                        new ShipNotFoundException(shipCode)
                );

        repository.delete(ship);
        publisher.publishShipDeleted(new ShipDeletedEvent(ship.getId()));
    }
    @Override
    @Transactional
    @Cacheable(value = "shipsById", key = "#shipId")
    public ShipResponse getById(Long shipId) {
        Ship ship = repository.findById(shipId)
                .orElseThrow(() -> new ShipNotFoundException(shipId));
        return mapper.toResponse(ship);
    }
}