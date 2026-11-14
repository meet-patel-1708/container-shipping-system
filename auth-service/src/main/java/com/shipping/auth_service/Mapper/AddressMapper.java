package com.shipping.auth_service.Mapper;

import com.shipping.auth_service.DTO.Request.AddressRequest;
import com.shipping.auth_service.DTO.Response.AddressResponse;
import com.shipping.auth_service.entity.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {
    public AddressResponse toResponse(Address address){
        return new AddressResponse(
                address.getAddressId(),
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getCountry(),
                address.getPincode(),
                address.getCreatedAt(),
                address.getUpdatedAt()
        );
    }
    public Address toEntity(AddressRequest request) {
        if (request == null) {
            return null;
        }
        return Address.builder()
                .street(request.street())
                .city(request.city())
                .state(request.state())
                .country(request.country())
                .pincode(request.pincode())
                .build();
    }
    public void toUpdate(AddressRequest request, Address address) {
        if (request == null || address == null) {
            return;
        }
        if (request.street() != null) {
            address.setStreet(request.street());
        }
        if (request.city() != null) {
            address.setCity(request.city());
        }
        if (request.state() != null) {
            address.setState(request.state());
        }
        if (request.country() != null) {
            address.setCountry(request.country());
        }
        if (request.pincode() != null) {
            address.setPincode(request.pincode());
        }
    }
}
