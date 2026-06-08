package com.org.archit.expensetracker.service;

import com.org.archit.expensetracker.dto.AuthDTO;
import com.org.archit.expensetracker.exception.DuplicateResourceException;
import com.org.archit.expensetracker.model.User;
import com.org.archit.expensetracker.repository.UserRepository;
import com.org.archit.expensetracker.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthDTO.AuthResponse register(AuthDTO.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        String token = jwtUtils.generateToken(user);

        return AuthDTO.AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .tokenType("Bearer")
                .build();
    }

    @Override
    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        String token = jwtUtils.generateToken(user);

        return AuthDTO.AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .tokenType("Bearer")
                .build();
    }
}
