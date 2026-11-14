package com.shipping.auth_service.Service;

import com.shipping.auth_service.DTO.Request.UpdateUserRequest;
import com.shipping.auth_service.DTO.Response.PagedResponse;
import com.shipping.auth_service.DTO.Response.UserResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse getById(Long userId);

    PagedResponse<UserResponse> list(Pageable pageable);

    UserResponse update(Long userId, UpdateUserRequest request);

    void delete(Long userId);
}
