package com.example.evsystem.service;

import com.example.evsystem.entity.Charger;
import com.example.evsystem.entity.ChargingSession;
import com.example.evsystem.entity.Reservation;
import com.example.evsystem.enums.ChargingSessionStatus;
import com.example.evsystem.enums.ChargerStatus;
import com.example.evsystem.enums.ReservationStatus;
import com.example.evsystem.exception.BusinessException;
import com.example.evsystem.repository.ChargerRepository;
import com.example.evsystem.repository.ChargingSessionRepository;
import com.example.evsystem.repository.ReservationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ChargingSessionService {

    private final ChargingSessionRepository chargingSessionRepository;
    private final ReservationRepository reservationRepository;
    private final ChargerRepository chargerRepository;

    public ChargingSessionService(ChargingSessionRepository chargingSessionRepository,
                                  ReservationRepository reservationRepository,
                                  ChargerRepository chargerRepository) {
        this.chargingSessionRepository = chargingSessionRepository;
        this.reservationRepository = reservationRepository;
        this.chargerRepository = chargerRepository;
    }

    public ChargingSession startSession(Long reservationId, Float startBatteryLevel) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Reservation could not be found: " + reservationId));
        validateBatteryLevel(startBatteryLevel, "Start battery level");

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.CONFLICT, "Only active reservations can start a charging session.");
        }

        Charger charger = chargerRepository.findByIdForUpdate(reservation.getCharger().getId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Charger could not be found: " + reservation.getCharger().getId()));

        chargingSessionRepository
                .findByReservationIdAndStatus(reservation.getId(), ChargingSessionStatus.ACTIVE)
                .ifPresent(s -> {
                    throw new BusinessException(HttpStatus.CONFLICT, "There is already an active session for this reservation.");
                });
        if (chargingSessionRepository.existsByReservation_Charger_IdAndStatus(charger.getId(), ChargingSessionStatus.ACTIVE)) {
            throw new BusinessException(HttpStatus.CONFLICT, "There is already an active session for this charger.");
        }
        if (charger.getStatus() != ChargerStatus.AVAILABLE) {
            throw new BusinessException(HttpStatus.CONFLICT, "Charger is not available for a new session.");
        }

        ChargingSession session = new ChargingSession();
        session.setReservation(reservation);
        session.setStartBatteryLevel(startBatteryLevel);
        session.setStartedAt(LocalDateTime.now());
        session.setStatus(ChargingSessionStatus.ACTIVE);
        charger.setStatus(ChargerStatus.OCCUPIED);
        return chargingSessionRepository.save(session);
    }

    public ChargingSession endSession(Long sessionId, Float endBatteryLevel, Float consumedKwh) {
        ChargingSession session = chargingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Session could not be found: " + sessionId));
        if (session.getStatus() != ChargingSessionStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.CONFLICT, "Session is not active.");
        }
        validateBatteryLevel(endBatteryLevel, "End battery level");
        if (consumedKwh == null || consumedKwh < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Consumed kWh must be zero or positive.");
        }

        Charger charger = chargerRepository.findByIdForUpdate(session.getReservation().getCharger().getId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Charger could not be found: " + session.getReservation().getCharger().getId()));

        Float pricePerKwh = session.getReservation().getCharger().getPricePerKwh();
        Float totalCost = consumedKwh * pricePerKwh;
        session.setEndBatteryLevel(endBatteryLevel);
        session.setConsumedKwh(consumedKwh);
        session.setTotalCost(totalCost);
        session.setEndedAt(LocalDateTime.now());
        session.setStatus(ChargingSessionStatus.COMPLETED);
        session.getReservation().setStatus(ReservationStatus.COMPLETED);
        charger.setStatus(ChargerStatus.AVAILABLE);
        return chargingSessionRepository.save(session);
    }

    public List<ChargingSession> getAllSessions() {
        return chargingSessionRepository.findAll();
    }

    public ChargingSession getSessionById(Long id) {
        return chargingSessionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Session could not be found: " + id));
    }

    private void validateBatteryLevel(Float batteryLevel, String fieldName) {
        if (batteryLevel == null || batteryLevel < 0 || batteryLevel > 100) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, fieldName + " must be between 0 and 100.");
        }
    }
}
