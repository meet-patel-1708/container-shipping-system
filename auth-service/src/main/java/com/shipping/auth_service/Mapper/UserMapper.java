package com.shipping.auth_service.Mapper;

import com.shipping.auth_service.DTO.Request.RegisterRequest;
import com.shipping.auth_service.DTO.Request.UpdateUserRequest;
import com.shipping.auth_service.DTO.Response.UserResponse;
import com.shipping.auth_service.entity.Role;
import com.shipping.auth_service.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {

        if (user == null) {
            return null;
        }

        return new UserResponse(
                user.getUserId(), user.getName(), user.getEmail(), user.getPhone(),
                user.getRole().name(), user.getStatus().name(),
                user.isEmailVerified(), user.isPhoneVerified(),
                user.getPosting(),
                user.getAssignedPortId(),
                user.getCreatedAt(), user.getUpdatedAt());
    }

    public User toEntity(RegisterRequest request) {

        if (request == null) {
            return null;
        }

        return User.builder()
                .name(request.name().trim())
                .email(request.email().trim().toLowerCase())
                .phone(request.phone().trim())
                .role(Role.USER)
                .build();
    }

    public void toUpdate(
            UpdateUserRequest request,
            User user
    ) {

        if (request == null || user == null) {
            return;
        }

        if (request.name() != null) {
            user.setName(request.name().trim());
        }

        if (request.phone() != null) {
            user.setPhone(request.phone().trim());
        }
    }
}