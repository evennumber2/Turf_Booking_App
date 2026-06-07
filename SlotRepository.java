package com.turf.booking.repository;

import com.turf.booking.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface SlotRepository extends JpaRepository<Slot, Long> {

    List<Slot> findByTurfIdAndSlotDateOrderByStartTime(Long turfId, LocalDate slotDate);

    Optional<Slot> findByTurfIdAndSlotDateAndStartTime(Long turfId, LocalDate slotDate, LocalTime startTime);
}
