package com.example.evsystem.service;

import com.example.evsystem.entity.ChargingSession;
import com.example.evsystem.entity.Reservation;
import com.example.evsystem.enums.ChargerStatus;
import com.example.evsystem.repository.ChargingSessionRepository;
import com.example.evsystem.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChargingSessionService {

    private final ChargingSessionRepository chargingSessionRepository;
    private final ReservationRepository reservationRepository;

    public ChargingSessionService(ChargingSessionRepository chargingSessionRepository,
                                  ReservationRepository reservationRepository) {
        this.chargingSessionRepository = chargingSessionRepository;
        this.reservationRepository = reservationRepository;
    }

    public ChargingSession startSession(Long reservationId, Float startBatteryLevel) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation could not be found: " + reservationId));
        chargingSessionRepository
                .findByReservationIdAndStatus(reservation.getId(), ChargerStatus.OCCUPIED)
                .ifPresent(s -> { throw new IllegalStateException("There is already an active session for this reservation."); });
        ChargingSession session = new ChargingSession();

        session.setReservation(reservation);
        session.setStartBatteryLevel(startBatteryLevel);
        session.setStartedAt(LocalDateTime.now());
        session.setStatus(ChargerStatus.OCCUPIED);
        return chargingSessionRepository.save(session);
    }

    public ChargingSession endSession(Long sessionId, Float endBatteryLevel, Float consumedKwh) {
        ChargingSession session = chargingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session could not be found: " + sessionId));
        if (session.getStatus() != ChargerStatus.OCCUPIED) {
            throw new IllegalStateException("Session is not active.");
        }
        // Calculate Total Cost
        Float pricePerKwh = session.getReservation().getCharger().getPricePerKwh();
        Float totalCost = consumedKwh * pricePerKwh;
        session.setEndBatteryLevel(endBatteryLevel);
        session.setConsumedKwh(consumedKwh);
        session.setTotalCost(totalCost);

        session.setEndedAt(LocalDateTime.now());
        session.setStatus(ChargerStatus.AVAILABLE);
        return chargingSessionRepository.save(session);
    }

    public List<ChargingSession> getAllSessions() {
        return chargingSessionRepository.findAll();
    }

    public ChargingSession getSessionById(Long id) {
        return chargingSessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Session bulunamadi: " + id));
    }
}