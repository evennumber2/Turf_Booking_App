package com.turf.booking.service;

import com.turf.booking.entity.Slot;
import com.turf.booking.entity.Turf;
import com.turf.booking.repository.SlotRepository;
import com.turf.booking.repository.TurfRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlotService {

    private final SlotRepository slotRepository;
    private final TurfRepository turfRepository;

    @Transactional
    public List<Slot> getSlots(Long turfId, LocalDate date) {
        validateDate(date);
        List<Slot> slots = slotRepository.findByTurfIdAndSlotDateOrderByStartTime(turfId, date);
        if (slots.isEmpty()) {
            generateSlots(turfId, date, 6, 22);
            slots = slotRepository.findByTurfIdAndSlotDateOrderByStartTime(turfId, date);
        }
        return slots;
    }

    @Transactional
    public List<Slot> generateSlots(Long turfId, LocalDate date, int startHour, int endHour) {
        validateDate(date);
        if (startHour < 0 || endHour > 24 || startHour >= endHour) {
            throw new IllegalArgumentException("Invalid slot hours");
        }

        Turf turf = turfRepository.findById(turfId)
                .orElseThrow(() -> new IllegalArgumentException("Turf not found"));

        List<Slot> created = new ArrayList<>();
        for (int hour = startHour; hour < endHour; hour++) {
            LocalTime start = LocalTime.of(hour, 0);
            boolean exists = slotRepository.findByTurfIdAndSlotDateAndStartTime(turfId, date, start).isPresent();
            if (!exists) {
                created.add(Slot.builder()
                        .turf(turf)
                        .slotDate(date)
                        .startTime(start)
                        .endTime(start.plusHours(1))
                        .status(Slot.Status.AVAILABLE)
                        .build());
            }
        }
        return slotRepository.saveAll(created);
    }

    private void validateDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date == null || date.isBefore(today) || date.isAfter(today.plusDays(7))) {
            throw new IllegalArgumentException("Choose a date from today up to 7 days ahead");
        }
    }
}
