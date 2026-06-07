package com.turf.booking.entity;

import com.turf.booking.config.StringListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "turfs")
public class Turf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String sport;

    @Column(length = 2000)
    private String description;

    @Column(length = 800)
    private String imageUrl;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal weekdayPricePerHour;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal weekendPricePerHour;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal lightsSurcharge;

    private Integer capacity;

    @Convert(converter = StringListConverter.class)
    @Column(length = 1200)
    @Builder.Default
    private List<String> amenities = new ArrayList<>();

    private Double latitude;

    private Double longitude;
}
