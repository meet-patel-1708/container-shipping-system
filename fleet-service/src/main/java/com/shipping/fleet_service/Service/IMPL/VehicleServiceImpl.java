package com.shipping.fleet_service.Service.IMPL;

import com.shipping.fleet_service.DTO.Request.VehicleRequest;
import com.shipping.fleet_service.DTO.Response.PagedResponse;
import com.shipping.fleet_service.DTO.Response.VehicleResponse;
import com.shipping.fleet_service.Entity.Vehicle;
import com.shipping.fleet_service.Event.VehicleCreatedEvent;
import com.shipping.fleet_service.Event.VehicleDeletedEvent;
import com.shipping.fleet_service.Event.VehicleEventPublisher;
import com.shipping.fleet_service.Event.VehicleUpdatedEvent;
import com.shipping.fleet_service.Exception.VehicleNotFoundException;
import com.shipping.fleet_service.Exception.VehicleNumberAlreadyExist;
import com.shipping.fleet_service.Mapper.VehicleMapper;
import com.shipping.fleet_service.Repository.VehicleRepository;
import com.shipping.fleet_service.Service.VehicleService;
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
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository repository;
    private final VehicleMapper mapper;
    private final VehicleEventPublisher publisher;

    @Override
    @Transactional
    public VehicleResponse create(VehicleRequest request) {

        if (repository.existsByVehicleNumber(request.vehicleNumber())) {
            throw new VehicleNumberAlreadyExist(
                    request.vehicleNumber()
            );
        }

        Vehicle vehicle = mapper.toEntity(request);

        Vehicle saved = repository.save(vehicle);
        publisher.publishVehicleCreated(new VehicleCreatedEvent(saved.getId(),saved.getVehicleName(),saved.getVehicleNumber(),saved.getVehicleType().name()));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    @Cacheable(value = "vehicles", key = "#vehicleId")
    public VehicleResponse getById(Long vehicleId) {

        Vehicle vehicle = repository.findById(vehicleId)
                .orElseThrow(() ->
                        new VehicleNotFoundException(vehicleId));

        return mapper.toResponse(vehicle);
    }

    @Override
    @Transactional
    @Cacheable(value = "vehicleByNumber", key = "#vehicleNumber")
    public VehicleResponse getByNumber(String vehicleNumber) {

        Vehicle vehicle = repository.findByVehicleNumber(vehicleNumber)
                .orElseThrow(() ->
                        new VehicleNotFoundException(vehicleNumber));

        return mapper.toResponse(vehicle);
    }

    @Override
    @Transactional
    public PagedResponse<VehicleResponse> list(Pageable pageable) {

        Page<VehicleResponse> page = repository.findAll(pageable)
                .map(mapper::toResponse);

        return PagedResponse.from(page);
    }

    @Override
    @Transactional
    public PagedResponse<VehicleResponse> searchByName(
            String vehicleName,
            Pageable pageable
    ) {

        Page<VehicleResponse> page =
                repository.findByVehicleNameContainingIgnoreCase(
                                vehicleName,
                                pageable
                        )
                        .map(mapper::toResponse);

        return PagedResponse.from(page);
    }

    @Override
    @Transactional
    @CachePut(value = "vehicles", key = "#vehicleNumber")
    public VehicleResponse update(
            String vehicleNumber,
            VehicleRequest request
    ) {

        Vehicle vehicle = repository.findByVehicleNumber(vehicleNumber)
                .orElseThrow(() ->
                        new VehicleNotFoundException(vehicleNumber));

        if (!vehicle.getVehicleNumber().equals(request.vehicleNumber())
                && repository.existsByVehicleNumber(request.vehicleNumber())) {

            throw new VehicleNumberAlreadyExist(
                    request.vehicleNumber()
            );
        }

        mapper.toUpdate(request, vehicle);

        Vehicle saved = repository.save(vehicle);
        publisher.publishVehicleUpdated(new VehicleUpdatedEvent(saved.getId(),saved.getVehicleName(),saved.getVehicleNumber(),saved.getVehicleType().name()));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "vehicles", key = "#vehicleNumber")
    public void delete(String vehicleNumber) {

        if (!repository.existsByVehicleNumber(vehicleNumber)) {
            throw new VehicleNotFoundException(vehicleNumber);
        }

        Vehicle vehicle = repository.findByVehicleNumber(vehicleNumber).orElseThrow(()->new VehicleNotFoundException(vehicleNumber));
        repository.delete(vehicle);
        publisher.publishVehicleDeleted(new VehicleDeletedEvent(vehicle.getId()));
    }
}