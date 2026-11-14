package com.shipping.auth_service.Event;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserRegisterEvent (
        Long userId,
        String name,
        String email,
        LocalDateTime registeredAt,
        String phone
){

}
