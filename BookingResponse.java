package com.turf.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private String bookingRef;
    private String turfName;
    private String location;
    private String sport;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal amount;
    private String transactionId;
    private String paymentStatus;
    private String bookingStatus;
}
