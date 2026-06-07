package com.turf.booking.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookingRequest {

    private Long turfId;
    private Long slotId;
    private BigDecimal amount;
    private String transactionId;
}
