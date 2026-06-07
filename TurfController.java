package com.turf.booking.controller;

import com.turf.booking.entity.Turf;
import com.turf.booking.service.TurfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/turfs")
@RequiredArgsConstructor
public class TurfController {

    private final TurfService turfService;

    @GetMapping
    public ResponseEntity<List<Turf>> list(@RequestParam(required = false) String city,
                                           @RequestParam(required = false) String sport) {
        return ResponseEntity.ok(turfService.search(city, sport));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Turf> get(@PathVariable Long id) {
        return ResponseEntity.ok(turfService.get(id));
    }

    @PostMapping
    public ResponseEntity<Turf> create(@RequestBody Turf turf) {
        return ResponseEntity.ok(turfService.create(turf));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Turf> update(@PathVariable Long id, @RequestBody Turf turf) {
        return ResponseEntity.ok(turfService.update(id, turf));
    }
}
