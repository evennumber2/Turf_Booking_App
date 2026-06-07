package com.turf.booking.service;

import com.turf.booking.entity.Turf;
import com.turf.booking.entity.User;
import com.turf.booking.repository.TurfRepository;
import com.turf.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final TurfRepository turfRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin();
        if (turfRepository.count() == 0) {
            turfRepository.saveAll(List.of(
                    turf("Skyline Cricket Arena", "Hyderabad", "Cricket", "Covered cricket turf with fresh nets, LED lights, and a lounge for teams.",
                            "https://images.unsplash.com/photo-1540747913346-19e32dc3e97e?auto=format&fit=crop&w=1200&q=80",
                            1200, 1500, 250, 18, List.of("Parking", "Changing rooms", "Drinking water", "First aid", "Restrooms"), 17.4375, 78.4483),
                    turf("Kicks Football Box", "Hyderabad", "Football", "Five-a-side football box with cushioned turf, rebound boards, and evening lights.",
                            "https://images.unsplash.com/photo-1522778119026-d647f0596c20?auto=format&fit=crop&w=1200&q=80",
                            1400, 1800, 300, 10, List.of("Parking", "Cafe", "Lights", "Restrooms", "Locker area"), 17.3850, 78.4867),
                    turf("Hoop City Indoor Court", "Bengaluru", "Basketball", "Indoor hardwood-style basketball court for hourly practice and friendly matches.",
                            "https://images.unsplash.com/photo-1546519638-68e109498ffc?auto=format&fit=crop&w=1200&q=80",
                            1000, 1300, 150, 12, List.of("Changing rooms", "Scoreboard", "Drinking water", "First aid"), 12.9716, 77.5946),
                    turf("Smash Badminton Hub", "Chennai", "Badminton", "Airy indoor badminton courts with marked lanes and non-slip flooring.",
                            "https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?auto=format&fit=crop&w=1200&q=80",
                            600, 800, 100, 4, List.of("Racquet rental", "Restrooms", "Drinking water", "Parking"), 13.0827, 80.2707),
                    turf("Ace Tennis Dome", "Pune", "Tennis", "Covered tennis court with coaching-friendly lighting and spectator seating.",
                            "https://images.unsplash.com/photo-1622279457486-62dcc4a431d6?auto=format&fit=crop&w=1200&q=80",
                            900, 1200, 200, 4, List.of("Parking", "Coach area", "Restrooms", "First aid"), 18.5204, 73.8567),
                    turf("Volley Max Court", "Mumbai", "Volleyball", "Indoor volleyball court with high ceiling clearance and bright court markings.",
                            "https://images.unsplash.com/photo-1612872087720-bb876e2e67d1?auto=format&fit=crop&w=1200&q=80",
                            1100, 1400, 200, 12, List.of("Changing rooms", "Drinking water", "Restrooms", "Parking"), 19.0760, 72.8777)
            ));
        }
    }

    private void seedAdmin() {
        if (userRepository.findByEmail("admin@turfbooking.local").isEmpty()) {
            userRepository.save(User.builder()
                    .name("Admin")
                    .email("admin@turfbooking.local")
                    .mobile("9999999999")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(User.Role.ADMIN)
                    .verified(true)
                    .build());
        }
    }

    private Turf turf(String name, String city, String sport, String description, String imageUrl,
                      int weekdayPrice, int weekendPrice, int lightsSurcharge, int capacity,
                      List<String> amenities, double latitude, double longitude) {
        return Turf.builder()
                .name(name)
                .city(city)
                .sport(sport)
                .description(description)
                .imageUrl(imageUrl)
                .weekdayPricePerHour(BigDecimal.valueOf(weekdayPrice))
                .weekendPricePerHour(BigDecimal.valueOf(weekendPrice))
                .lightsSurcharge(BigDecimal.valueOf(lightsSurcharge))
                .capacity(capacity)
                .amenities(amenities)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}
