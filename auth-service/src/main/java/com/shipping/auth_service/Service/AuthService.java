package com.shipping.auth_service.Service;

import com.shipping.auth_service.DTO.Request.LoginRequest;
import com.shipping.auth_service.DTO.Request.RegisterRequest;
import com.shipping.auth_service.DTO.Response.AuthResponse;
import com.shipping.auth_service.DTO.Response.RegisterResponse;


public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(String refreshToken);
}