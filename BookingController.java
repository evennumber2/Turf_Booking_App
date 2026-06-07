package com.turf.booking.controller;

import com.turf.booking.dto.BookingRequest;
import com.turf.booking.dto.BookingResponse;
import com.turf.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> book(@RequestBody BookingRequest request, Authentication authentication) {
        return ResponseEntity.ok(bookingService.book(request, authentication.getName()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> myBookings(Authentication authentication) {
        return ResponseEntity.ok(bookingService.myBookings(authentication.getName()));
    }

    @GetMapping("/{ref}")
    public ResponseEntity<BookingResponse> getByRef(@PathVariable String ref, Authentication authentication) {
        return ResponseEntity.ok(bookingService.getByRef(ref, authentication.getName()));
    }
}
