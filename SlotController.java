package com.turf.booking.controller;

import com.turf.booking.entity.Slot;
import com.turf.booking.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;

    @GetMapping("/turf/{turfId}")
    public ResponseEntity<List<Map<String, Object>>> getSlots(
            @PathVariable Long turfId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(slotService.getSlots(turfId, date).stream().map(this::slotMap).toList());
    }

    @PostMapping("/generate")
    public ResponseEntity<List<Map<String, Object>>> generate(
            @RequestParam Long turfId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "6") int startHour,
            @RequestParam(defaultValue = "22") int endHour
    ) {
        return ResponseEntity.ok(slotService.generateSlots(turfId, date, startHour, endHour)
                .stream()
                .map(this::slotMap)
                .toList());
    }

    private Map<String, Object> slotMap(Slot slot) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("id", slot.getId());
        value.put("slotDate", slot.getSlotDate());
        value.put("startTime", slot.getStartTime());
        value.put("endTime", slot.getEndTime());
        value.put("status", slot.getStatus().name());
        return value;
    }
}
