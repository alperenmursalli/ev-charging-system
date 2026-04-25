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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
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
        validateReservationWindow(reservation);

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

    public ChargingSession endSession(Long sessionId, Float endBatteryLevel) {
        ChargingSession session = chargingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Session could not be found: " + sessionId));
        if (session.getStatus() != ChargingSessionStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.CONFLICT, "Session is not active.");
        }
        validateBatteryLevel(endBatteryLevel, "End battery level");
        if (endBatteryLevel < session.getStartBatteryLevel()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "End battery level cannot be lower than the start battery level.");
        }

        return completeSession(session, endBatteryLevel, calculateConsumedKwh(session, endBatteryLevel), LocalDateTime.now());
    }

    @Scheduled(fixedDelayString = "${charging.session.auto-end-delay-ms:60000}")
    public void autoCompleteExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        List<ChargingSession> expiredSessions = chargingSessionRepository
                .findByStatusAndReservation_EndTimeLessThanEqual(ChargingSessionStatus.ACTIVE, now);

        for (ChargingSession session : expiredSessions) {
            completeSession(session, estimateAutoEndBatteryLevel(session, now), estimateAutoConsumedKwh(session, now), now);
        }
    }

    @Transactional(readOnly = true)
    public List<ChargingSession> getAllSessions() {
        return chargingSessionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ChargingSession getSessionById(Long id) {
        return chargingSessionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Session could not be found: " + id));
    }

    private ChargingSession completeSession(ChargingSession session, Float endBatteryLevel, Float consumedKwh, LocalDateTime endedAt) {
        Charger charger = chargerRepository.findByIdForUpdate(session.getReservation().getCharger().getId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Charger could not be found: " + session.getReservation().getCharger().getId()));

        Float pricePerKwh = session.getReservation().getCharger().getPricePerKwh();
        Float totalCost = round(consumedKwh * pricePerKwh);
        session.setEndBatteryLevel(endBatteryLevel);
        session.setConsumedKwh(consumedKwh);
        session.setTotalCost(totalCost);
        session.setEndedAt(endedAt);
        session.setStatus(ChargingSessionStatus.COMPLETED);
        session.getReservation().setStatus(ReservationStatus.COMPLETED);
        charger.setStatus(ChargerStatus.AVAILABLE);
        return chargingSessionRepository.save(session);
    }

    private void validateBatteryLevel(Float batteryLevel, String fieldName) {
        if (batteryLevel == null || batteryLevel < 0 || batteryLevel > 100) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, fieldName + " must be between 0 and 100.");
        }
    }

    private Float calculateConsumedKwh(ChargingSession session, Float endBatteryLevel) {
        Double batteryCapacity = session.getReservation().getVehicle().getBatteryCapacity();
        if (batteryCapacity == null || batteryCapacity <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Vehicle battery capacity must be greater than zero.");
        }

        float chargedPercentage = endBatteryLevel - session.getStartBatteryLevel();
        return round((float) (batteryCapacity * chargedPercentage / 100d));
    }

    private Float estimateAutoConsumedKwh(ChargingSession session, LocalDateTime endedAt) {
        double availableCapacityKwh = calculateAvailableCapacityKwh(session);
        double maxDeliverableKwh = calculateMaxDeliverableKwh(session, endedAt);
        return round((float) Math.min(availableCapacityKwh, maxDeliverableKwh));
    }

    private Float estimateAutoEndBatteryLevel(ChargingSession session, LocalDateTime endedAt) {
        Double batteryCapacity = session.getReservation().getVehicle().getBatteryCapacity();
        if (batteryCapacity == null || batteryCapacity <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Vehicle battery capacity must be greater than zero.");
        }

        float consumedKwh = estimateAutoConsumedKwh(session, endedAt);
        float chargedPercentage = round((float) (consumedKwh * 100d / batteryCapacity));
        return round(Math.min(100f, session.getStartBatteryLevel() + chargedPercentage));
    }

    private double calculateAvailableCapacityKwh(ChargingSession session) {
        Double batteryCapacity = session.getReservation().getVehicle().getBatteryCapacity();
        if (batteryCapacity == null || batteryCapacity <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Vehicle battery capacity must be greater than zero.");
        }

        return batteryCapacity * (100d - session.getStartBatteryLevel()) / 100d;
    }

    private double calculateMaxDeliverableKwh(ChargingSession session, LocalDateTime endedAt) {
        if (session.getStartedAt() == null) {
            return 0d;
        }

        Duration duration = Duration.between(session.getStartedAt(), endedAt);
        double hours = Math.max(0d, duration.toMillis() / 3_600_000d);
        return session.getReservation().getCharger().getPowerOutput().getValue() * hours;
    }

    private Float round(float value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .floatValue();
    }

    private void validateReservationWindow(Reservation reservation) {
        LocalDateTime now = LocalDateTime.now();

        if (reservation.getStartTime() != null && now.isBefore(reservation.getStartTime())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Charging session cannot start before the reservation window.");
        }

        if (reservation.getEndTime() != null && !now.isBefore(reservation.getEndTime())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Charging session cannot start after the reservation end time.");
        }
    }
}
