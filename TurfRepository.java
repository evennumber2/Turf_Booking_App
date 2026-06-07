package com.turf.booking.repository;

import com.turf.booking.entity.Turf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TurfRepository extends JpaRepository<Turf, Long> {

    @Query("""
            select t from Turf t
            where (:city is null or lower(t.city) like lower(concat('%', :city, '%')))
              and (:sport is null or lower(t.sport) = lower(:sport))
            order by t.weekdayPricePerHour asc
            """)
    List<Turf> search(@Param("city") String city, @Param("sport") String sport);
}
