package com.example.evsystem.repository;

import com.example.evsystem.entity.ChargingSession;
import com.example.evsystem.enums.ChargingSessionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChargingSessionRepository extends JpaRepository<ChargingSession, Long> {

    Optional<ChargingSession> findByReservationIdAndStatus(Long reservationId, ChargingSessionStatus status);
    boolean existsByReservation_Charger_IdAndStatus(Long chargerId, ChargingSessionStatus status);
    List<ChargingSession> findByReservation_Vehicle_Id(Long vehicleId);
    List<ChargingSession> findByReservation_Charger_Id(Long chargerId);
    @EntityGraph(attributePaths = {"reservation", "reservation.charger", "reservation.vehicle"})
    List<ChargingSession> findByStatusAndReservation_EndTimeLessThanEqual(ChargingSessionStatus status, LocalDateTime endTime);

    @Override
    @EntityGraph(attributePaths = {"reservation", "reservation.charger", "reservation.vehicle"})
    List<ChargingSession> findAll();

    @Override
    @EntityGraph(attributePaths = {"reservation", "reservation.charger", "reservation.vehicle"})
    Optional<ChargingSession> findById(Long id);
}
