package com.shipping.auth_service.Event;

public record UserUpdatedEvent(Long userId,String name,String email,String phone) {
}
