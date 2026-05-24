package com.gsp.aiims.auth.service;

import com.gsp.aiims.auth.dto.LoginRequest;
import com.gsp.aiims.auth.dto.LoginResponse;
import com.gsp.aiims.auth.dto.RefreshTokenRequest;
import com.gsp.aiims.auth.dto.RegisterRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse register(RegisterRequest request);

    LoginResponse refreshToken(RefreshTokenRequest request);

    void logout(String refreshToken);
}
