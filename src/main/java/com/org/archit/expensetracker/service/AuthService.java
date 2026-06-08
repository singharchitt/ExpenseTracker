package com.org.archit.expensetracker.service;

import com.org.archit.expensetracker.dto.AuthDTO;

public interface AuthService {
    AuthDTO.AuthResponse register(AuthDTO.RegisterRequest request);
    AuthDTO.AuthResponse login(AuthDTO.LoginRequest request);
}
