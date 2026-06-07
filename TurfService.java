package com.turf.booking.service;

import com.turf.booking.entity.Turf;
import com.turf.booking.repository.TurfRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TurfService {

    private final TurfRepository turfRepository;

    public List<Turf> search(String city, String sport) {
        return turfRepository.search(blankToNull(city), blankToNull(sport));
    }

    public Turf get(Long id) {
        return turfRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Turf not found"));
    }

    public Turf create(Turf turf) {
        return turfRepository.save(turf);
    }

    public Turf update(Long id, Turf request) {
        Turf turf = get(id);
        turf.setName(request.getName());
        turf.setCity(request.getCity());
        turf.setSport(request.getSport());
        turf.setDescription(request.getDescription());
        turf.setImageUrl(request.getImageUrl());
        turf.setWeekdayPricePerHour(request.getWeekdayPricePerHour());
        turf.setWeekendPricePerHour(request.getWeekendPricePerHour());
        turf.setLightsSurcharge(request.getLightsSurcharge());
        turf.setCapacity(request.getCapacity());
        turf.setAmenities(request.getAmenities());
        turf.setLatitude(request.getLatitude());
        turf.setLongitude(request.getLongitude());
        return turfRepository.save(turf);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
