package com.turf.booking.controller;

import com.turf.booking.dto.AuthResponse;
import com.turf.booking.dto.LoginRequest;
import com.turf.booking.dto.OtpRequest;
import com.turf.booking.dto.RegisterRequest;
import com.turf.booking.service.AuthService;
import com.turf.booking.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>> sendOtp(@RequestBody OtpRequest request) {
        otpService.sendOtp(request.destinationValue());
        return ResponseEntity.ok(Map.of("message", "OTP sent. Check the Spring Boot console."));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Boolean>> verifyOtp(@RequestBody OtpRequest request) {
        boolean verified = otpService.verifyOtp(request.destinationValue(), request.getOtp());
        return ResponseEntity.ok(Map.of("verified", verified));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody OtpRequest request) {
        authService.forgotPassword(request.destinationValue());
        return ResponseEntity.ok(Map.of("message", "Reset link sent. Check the Spring Boot console."));
    }
}
