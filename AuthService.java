package com.turf.booking.service;

import com.turf.booking.config.JwtUtils;
import com.turf.booking.dto.AuthResponse;
import com.turf.booking.dto.LoginRequest;
import com.turf.booking.dto.RegisterRequest;
import com.turf.booking.entity.User;
import com.turf.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final OtpService otpService;

    public AuthResponse register(RegisterRequest request) {
        String name = text(request.getName());
        String email = text(request.getEmail()).toLowerCase();
        String mobile = text(request.getMobile());

        if (name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (email.isBlank() && mobile.isBlank()) {
            throw new IllegalArgumentException("Email or mobile is required");
        }
        if (!mobile.isBlank() && !mobile.matches("\\d{10}")) {
            throw new IllegalArgumentException("Mobile number must be 10 digits");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (!email.isBlank() && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }
        if (!mobile.isBlank() && userRepository.existsByMobile(mobile)) {
            throw new IllegalArgumentException("Mobile is already registered");
        }
        boolean otpVerified = otpService.consumeVerification(email) || otpService.consumeVerification(mobile);
        if (!otpVerified) {
            throw new IllegalArgumentException("Please verify OTP before registering");
        }

        User user = User.builder()
                .name(name)
                .email(email.isBlank() ? null : email)
                .mobile(mobile.isBlank() ? null : mobile)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .verified(true)
                .build();

        return authResponse(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        String identifier = text(request.getIdentifier()).toLowerCase();
        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return authResponse(user);
    }

    public void forgotPassword(String destination) {
        String key = text(destination);
        if (key.isBlank()) {
            throw new IllegalArgumentException("Email or mobile is required");
        }
        String resetLink = "http://localhost:8080/index.html?reset=" + Instant.now().toEpochMilli();
        System.out.printf("Password reset link for %s: %s%n", key, resetLink);
    }

    private AuthResponse authResponse(User user) {
        return AuthResponse.builder()
                .token(jwtUtils.generateToken(user))
                .name(user.getName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .build();
    }

    private String text(String value) {
        return value == null ? "" : value.trim();
    }
}
