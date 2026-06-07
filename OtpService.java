package com.turf.booking.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final long OTP_TTL_SECONDS = 300;

    private final SecureRandom random = new SecureRandom();
    private final Map<String, OtpEntry> otps = new ConcurrentHashMap<>();
    private final Map<String, Instant> verifiedDestinations = new ConcurrentHashMap<>();

    public void sendOtp(String destination) {
        String key = normalize(destination);
        if (key.isBlank()) {
            throw new IllegalArgumentException("Email or mobile is required");
        }

        String otp = String.format("%06d", random.nextInt(1_000_000));
        otps.put(key, new OtpEntry(otp, Instant.now().plusSeconds(OTP_TTL_SECONDS)));
        System.out.printf("OTP for %s is %s%n", key, otp);
    }

    public boolean verifyOtp(String destination, String otp) {
        String key = normalize(destination);
        OtpEntry entry = otps.get(key);

        if (entry == null || entry.expiresAt().isBefore(Instant.now())) {
            otps.remove(key);
            return false;
        }

        boolean verified = entry.otp().equals(otp);
        if (verified) {
            otps.remove(key);
            verifiedDestinations.put(key, Instant.now().plusSeconds(OTP_TTL_SECONDS));
        }
        return verified;
    }

    public boolean consumeVerification(String destination) {
        String key = normalize(destination);
        Instant expiresAt = verifiedDestinations.get(key);
        if (expiresAt == null || expiresAt.isBefore(Instant.now())) {
            verifiedDestinations.remove(key);
            return false;
        }
        verifiedDestinations.remove(key);
        return true;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private record OtpEntry(String otp, Instant expiresAt) {
    }
}
