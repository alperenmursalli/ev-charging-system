package com.example.evsystem.repository;

import com.example.evsystem.entity.ChargingSession;
import com.example.evsystem.enums.ChargerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChargingSessionRepository extends JpaRepository<ChargingSession, Long> {

    Optional<ChargingSession> findByReservationIdAndStatus(Long reservationId, ChargerStatus status);
    List<ChargingSession> findByReservation_Vehicle_Id(Long vehicleId);
    List<ChargingSession> findByReservation_Charger_Id(Long chargerId);
}