package com.shipping.booking_service.service.impl;

import com.shipping.booking_service.client.AuthServiceClient;
import com.shipping.booking_service.client.CatalogServiceClient;
import com.shipping.booking_service.client.FleetServiceClient;
import com.shipping.booking_service.repository.Replica.*;
import com.shipping.booking_service.service.ReferenceValidationService;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReferenceValidationServiceImpl implements ReferenceValidationService {
    private final AuthServiceClient authServiceClient;
    private final FleetServiceClient fleetServiceClient;
    private final CatalogServiceClient catalogServiceClient;

    private final UserReplicaRepository userReplicaRepository;
    private final ShipReplicaRepository shipReplicaRepository;
    private final VehicleReplicaRepository vehicleReplicaRepository;
    private final PortReplicaRepository portReplicaRepository;
    private final ProductReplicaRepository productReplicaRepository;

    @Override
    @CircuitBreaker(name = "authService",fallbackMethod = "fallbackUser")
    public boolean isUserValid(Long userId) {
        try {
            authServiceClient.getuser(userId);
            return true;
        } catch (FeignException.NotFound e) {
            return false; // real 404 - definitely doesn't exist, not a downstream failure
        }

    }
    private boolean fallbackUser(Long userId, Throwable t) {
        log.warn("auth-service unreachable, falling back to local replica for userId={}. Reason: {}", userId, t.getMessage());
        return userReplicaRepository.existsById(userId);
    }
    @Override
    @CircuitBreaker(name = "shipService",fallbackMethod = "fallbackShip")
    public boolean isShipValid(Long shipId) {
        try {
            fleetServiceClient.getShip(shipId);
            return true;
        } catch (FeignException.NotFound e) {
            return false;
        }
    }
    private boolean fallbackShip(Long shipId, Throwable t) {
        log.warn("fleet-service unreachable, falling back to local replica for shipId={}. Reason: {}", shipId, t.getMessage());
        return shipReplicaRepository.existsById(shipId);
    }
    @Override
    @CircuitBreaker(name = "fleetService", fallbackMethod = "fallbackVehicle")
    public boolean isVehicleValid(Long vehicleId) {
        try {
            fleetServiceClient.getVehicle(vehicleId);
            return true;
        } catch (FeignException.NotFound e) {
            return false;
        }
    }
    private boolean fallbackVehicle(Long vehicleId, Throwable t) {
        log.warn("fleet-service unreachable, falling back to local replica for vehicleId={}. Reason: {}", vehicleId, t.getMessage());
        return vehicleReplicaRepository.existsById(vehicleId);
    }
    @Override
    @CircuitBreaker(name = "fleetService", fallbackMethod = "fallbackPort")
    public boolean isPortValid(Long portId) {
        try {
            fleetServiceClient.getPort(portId);
            return true;
        } catch (FeignException.NotFound e) {
            return false;
        }
    }
    private boolean fallbackPort(Long portId, Throwable t) {
        log.warn("fleet-service unreachable, falling back to local replica for portId={}. Reason: {}", portId, t.getMessage());
        return portReplicaRepository.existsById(portId);
    }

    @Override
    @CircuitBreaker(name = "catalogService", fallbackMethod = "fallbackProduct")
    public boolean isProductValid(Long productId) {
        try {
            catalogServiceClient.getProduct(productId);
            return true;
        } catch (FeignException.NotFound e) {
            return false;
        }
    }

    private boolean fallbackProduct(Long productId, Throwable t) {
        log.warn("catalog-service unreachable, falling back to local replica for productId={}. Reason: {}", productId, t.getMessage());
        return productReplicaRepository.existsById(productId);
    }
}
