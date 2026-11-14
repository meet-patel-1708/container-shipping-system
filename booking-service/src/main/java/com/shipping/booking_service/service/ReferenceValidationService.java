package com.shipping.booking_service.service;

public interface ReferenceValidationService {

    boolean isUserValid(Long userId);

    boolean isShipValid(Long shipId);

    boolean isVehicleValid(Long vehicleId);

    boolean isPortValid(Long portId);

    boolean isProductValid(Long productId);
}