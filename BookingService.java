package com.turf.booking.service;

import com.turf.booking.dto.BookingRequest;
import com.turf.booking.dto.BookingResponse;
import com.turf.booking.entity.Booking;
import com.turf.booking.entity.Slot;
import com.turf.booking.entity.User;
import com.turf.booking.repository.BookingRepository;
import com.turf.booking.repository.SlotRepository;
import com.turf.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final SlotRepository slotRepository;

    @Transactional
    public BookingResponse book(BookingRequest request, String identifier) {
        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Slot slot = slotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new IllegalArgumentException("Slot not found"));

        if (!slot.getTurf().getId().equals(request.getTurfId())) {
            throw new IllegalArgumentException("Slot does not belong to this turf");
        }
        if (slot.getStatus() != Slot.Status.AVAILABLE) {
            throw new IllegalArgumentException("This slot is already booked");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        slot.setStatus(Slot.Status.BOOKED);
        Booking booking = Booking.builder()
                .bookingRef(nextBookingRef())
                .user(user)
                .slot(slot)
                .turf(slot.getTurf())
                .amount(request.getAmount())
                .paymentStatus(Booking.PaymentStatus.PAID)
                .bookingStatus(Booking.BookingStatus.CONFIRMED)
                .transactionId(request.getTransactionId())
                .build();

        return toResponse(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> myBookings(String identifier) {
        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getByRef(String ref, String identifier) {
        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Booking booking = bookingRepository.findByBookingRef(ref)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (!booking.getUser().getId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("You cannot view this booking");
        }
        return toResponse(booking);
    }

    private String nextBookingRef() {
        String ref;
        do {
            ref = "TB-" + DateTimeFormatter.BASIC_ISO_DATE.format(java.time.LocalDate.now())
                    + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } while (bookingRepository.existsByBookingRef(ref));
        return ref;
    }

    private BookingResponse toResponse(Booking booking) {
        Slot slot = booking.getSlot();
        return BookingResponse.builder()
                .bookingRef(booking.getBookingRef())
                .turfName(booking.getTurf().getName())
                .location(booking.getTurf().getCity())
                .sport(booking.getTurf().getSport())
                .date(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .amount(booking.getAmount())
                .transactionId(booking.getTransactionId())
                .paymentStatus(booking.getPaymentStatus().name())
                .bookingStatus(booking.getBookingStatus().name())
                .build();
    }
}
