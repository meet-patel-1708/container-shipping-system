package com.shipping.booking_service.event.incoming.UserEvent;

import java.time.LocalDateTime;

public record UserUpdateEvent(Long userId, String name, String email, LocalDateTime registeredAt) {}