package com.example.evsystem.repository;

import com.example.evsystem.entity.Reservation;
import com.example.evsystem.enums.ReservationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByChargerIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
            Long chargerId,
            Collection<ReservationStatus> statuses,
            LocalDateTime endTime,
            LocalDateTime startTime
    );

    @EntityGraph(attributePaths = {"vehicle", "charger"})
    List<Reservation> findByStatusAndStartTimeLessThanEqualAndEndTimeGreaterThan(
            ReservationStatus status,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
}
