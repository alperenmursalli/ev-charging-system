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
    boolean existsByReservationIdAndStatus(Long reservationId, ChargingSessionStatus status);
    boolean existsByReservationId(Long reservationId);
    boolean existsByReservation_Vehicle_Id(Long vehicleId);
    boolean existsByReservation_Charger_Id(Long chargerId);
    boolean existsByReservation_Charger_Station_Id(Long stationId);
    boolean existsByReservation_Charger_IdAndStatus(Long chargerId, ChargingSessionStatus status);
    List<ChargingSession> findByReservation_Vehicle_Id(Long vehicleId);
    @EntityGraph(attributePaths = {"reservation", "reservation.charger", "reservation.vehicle"})
    List<ChargingSession> findByReservation_Vehicle_Owner_UsernameIgnoreCase(String username);
    List<ChargingSession> findByReservation_Charger_Id(Long chargerId);
    Optional<ChargingSession> findTopByReservation_IdOrderByStartedAtDescIdDesc(Long reservationId);
    Optional<ChargingSession> findTopByReservation_Vehicle_IdOrderByStartedAtDescIdDesc(Long vehicleId);
    @EntityGraph(attributePaths = {"reservation", "reservation.charger", "reservation.vehicle"})
    List<ChargingSession> findByStatusAndReservation_EndTimeLessThanEqual(ChargingSessionStatus status, LocalDateTime endTime);

    @Override
    @EntityGraph(attributePaths = {"reservation", "reservation.charger", "reservation.vehicle"})
    List<ChargingSession> findAll();

    @Override
    @EntityGraph(attributePaths = {"reservation", "reservation.charger", "reservation.vehicle"})
    Optional<ChargingSession> findById(Long id);
}
