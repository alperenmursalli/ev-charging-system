package com.example.evsystem.repository;

import com.example.evsystem.entity.Reservation;
import com.example.evsystem.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByChargerIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
            Long chargerId,
            Collection<ReservationStatus> statuses,
            LocalDateTime endTime,
            LocalDateTime startTime
    );
}
