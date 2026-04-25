package com.example.evsystem.service;

import com.example.evsystem.dto.CreateReservationRequest;
import com.example.evsystem.dto.ReservationResponse;
import com.example.evsystem.entity.Charger;
import com.example.evsystem.entity.Reservation;
import com.example.evsystem.entity.Vehicle;
import com.example.evsystem.enums.ChargerStatus;
import com.example.evsystem.enums.ConnectorType;
import com.example.evsystem.enums.ReservationStatus;
import com.example.evsystem.exception.BusinessException;
import com.example.evsystem.repository.ChargerRepository;
import com.example.evsystem.repository.ChargingSessionRepository;
import com.example.evsystem.repository.ReservationRepository;
import com.example.evsystem.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private static final Duration MAX_RESERVATION_DURATION = Duration.ofHours(2);
    private static final Duration MAX_ADVANCE_WINDOW = Duration.ofHours(24);

    private final ReservationRepository reservationRepository;
    private final VehicleRepository vehicleRepository;
    private final ChargerRepository chargerRepository;
    private final ChargingSessionRepository chargingSessionRepository;
    private final Duration autoDeleteRetention;

    public ReservationService(
            ReservationRepository reservationRepository,
            VehicleRepository vehicleRepository,
            ChargerRepository chargerRepository,
            ChargingSessionRepository chargingSessionRepository,
            @Value("${reservation.auto-delete-retention-hours:24}") long autoDeleteRetentionHours
    ) {
        this.reservationRepository = reservationRepository;
        this.vehicleRepository = vehicleRepository;
        this.chargerRepository = chargerRepository;
        this.chargingSessionRepository = chargingSessionRepository;
        this.autoDeleteRetention = Duration.ofHours(autoDeleteRetentionHours);
    }

    public Reservation create(CreateReservationRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Vehicle not found."));
        Charger charger = chargerRepository.findByIdForUpdate(request.getChargerId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Charger not found."));

        validateTimes(request.getStartTime(), request.getEndTime());
        validateConnectorCompatibility(vehicle, charger);
        validateChargerAvailability(charger);
        validateNoOverlap(request.getChargerId(), request.getStartTime(), request.getEndTime());

        Reservation reservation = new Reservation();
        reservation.setVehicle(vehicle);
        reservation.setCharger(charger);
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setStatus(ReservationStatus.ACTIVE);
        return reservationRepository.save(reservation);
    }

    public ReservationResponse createResponse(CreateReservationRequest request) {
        return ReservationResponse.from(create(request));
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllResponses() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReservationResponse getResponseById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Reservation not found."));
        return ReservationResponse.from(reservation);
    }

    private void validateTimes(LocalDateTime startTime, LocalDateTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Reservation end time must be after start time.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (startTime.isBefore(now)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Reservation start time cannot be in the past.");
        }

        if (startTime.isAfter(now.plus(MAX_ADVANCE_WINDOW))) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Reservations can only be created within the next 24 hours.");
        }

        Duration duration = Duration.between(startTime, endTime);
        if (duration.compareTo(MAX_RESERVATION_DURATION) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Reservation duration cannot exceed 2 hours.");
        }
    }

    public void cancel(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Reservation not found."));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BusinessException(HttpStatus.CONFLICT, "Reservation is already cancelled.");
        }

        if (reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new BusinessException(HttpStatus.CONFLICT, "Completed reservations cannot be cancelled.");
        }

        if (reservation.getStatus() == ReservationStatus.EXPIRED) {
            throw new BusinessException(HttpStatus.CONFLICT, "Expired reservations cannot be cancelled.");
        }

        if (reservation.getStatus() == ReservationStatus.IN_PROGRESS ||
                chargingSessionRepository.existsByReservationIdAndStatus(id, com.example.evsystem.enums.ChargingSessionStatus.ACTIVE)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Reservations with an active charging session cannot be cancelled.");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    public void delete(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Reservation not found."));

        if (reservation.getStatus() == ReservationStatus.IN_PROGRESS ||
                chargingSessionRepository.existsByReservationIdAndStatus(id, com.example.evsystem.enums.ChargingSessionStatus.ACTIVE)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Reservations with an active charging session cannot be deleted.");
        }

        if (chargingSessionRepository.existsByReservationId(id)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Reservations with charging session history cannot be deleted.");
        }

        reservationRepository.delete(reservation);
    }

    public void markInProgress(Long reservationId) {
        updateStatus(reservationId, ReservationStatus.IN_PROGRESS);
    }

    public void markCompleted(Long reservationId) {
        updateStatus(reservationId, ReservationStatus.COMPLETED);
    }

    @Scheduled(fixedDelayString = "${reservation.auto-expire-delay-ms:30000}")
    public void autoExpireReservationsWithoutSession() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> expiredReservations = reservationRepository.findByStatusInAndEndTimeLessThanEqual(
                List.of(ReservationStatus.ACTIVE),
                now
        );

        for (Reservation reservation : expiredReservations) {
            if (chargingSessionRepository.existsByReservationIdAndStatus(reservation.getId(), com.example.evsystem.enums.ChargingSessionStatus.ACTIVE)) {
                continue;
            }

            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);
            log.info("Auto-expired reservation {}", reservation.getId());
        }
    }

    @Scheduled(fixedDelayString = "${reservation.auto-delete-delay-ms:3600000}")
    public void autoDeleteTerminalReservations() {
        LocalDateTime threshold = LocalDateTime.now().minus(autoDeleteRetention);
        List<Reservation> deletableReservations = reservationRepository.findByStatusInAndEndTimeBefore(
                List.of(ReservationStatus.CANCELLED, ReservationStatus.COMPLETED, ReservationStatus.EXPIRED),
                threshold
        );

        for (Reservation reservation : deletableReservations) {
            if (chargingSessionRepository.existsByReservationId(reservation.getId())) {
                continue;
            }

            reservationRepository.delete(reservation);
            log.info("Auto-deleted reservation {}", reservation.getId());
        }
    }

    private void validateConnectorCompatibility(Vehicle vehicle, Charger charger) {
        ConnectorType chargerConnectorType = charger.getConnectorType();
        if (chargerConnectorType == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Charger connector type is required.");
        }

        if (vehicle.getConnectorType() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Vehicle connector type is required.");
        }

        if (vehicle.getConnectorType() != chargerConnectorType) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Vehicle connector type is not compatible with the charger.");
        }
    }

    private void validateNoOverlap(Long chargerId, LocalDateTime startTime, LocalDateTime endTime) {
        boolean overlaps = reservationRepository.existsByChargerIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                chargerId,
                List.of(ReservationStatus.ACTIVE, ReservationStatus.IN_PROGRESS),
                endTime,
                startTime
        );

        if (overlaps) {
            throw new BusinessException(HttpStatus.CONFLICT, "There is already a reservation for this charger in the selected time range.");
        }
    }

    private void validateChargerAvailability(Charger charger) {
        if (charger.getStatus() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Charger status is required.");
        }

        if (charger.getStatus() != ChargerStatus.AVAILABLE) {
            throw new BusinessException(HttpStatus.CONFLICT, "Reservations can only be created for available chargers.");
        }
    }

    private void updateStatus(Long reservationId, ReservationStatus status) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Reservation not found."));
        reservation.setStatus(status);
        reservationRepository.save(reservation);
    }
}
