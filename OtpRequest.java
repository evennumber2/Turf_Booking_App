package com.turf.booking.dto;

import lombok.Data;

@Data
public class OtpRequest {

    private String destination;
    private String email;
    private String mobile;
    private String otp;

    public String destinationValue() {
        if (destination != null && !destination.isBlank()) {
            return destination.trim();
        }
        if (email != null && !email.isBlank()) {
            return email.trim();
        }
        if (mobile != null && !mobile.isBlank()) {
            return mobile.trim();
        }
        return "";
    }
}
