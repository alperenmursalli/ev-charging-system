package com.example.evsystem.service;

import com.example.evsystem.dto.CreateReservationRequest;
import com.example.evsystem.entity.Charger;
import com.example.evsystem.entity.Reservation;
import com.example.evsystem.entity.Vehicle;
import com.example.evsystem.enums.ConnectorType;
import com.example.evsystem.enums.ReservationStatus;
import com.example.evsystem.exception.BusinessException;
import com.example.evsystem.repository.ChargerRepository;
import com.example.evsystem.repository.ReservationRepository;
import com.example.evsystem.repository.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ReservationService {

    private static final Duration MAX_RESERVATION_DURATION = Duration.ofHours(2);
    private static final Duration MAX_ADVANCE_WINDOW = Duration.ofHours(24);

    private final ReservationRepository reservationRepository;
    private final VehicleRepository vehicleRepository;
    private final ChargerRepository chargerRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            VehicleRepository vehicleRepository,
            ChargerRepository chargerRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.vehicleRepository = vehicleRepository;
        this.chargerRepository = chargerRepository;
    }

    public Reservation create(CreateReservationRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Vehicle not found."));
        Charger charger = chargerRepository.findById(request.getChargerId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Charger not found."));

        validateTimes(request.getStartTime(), request.getEndTime());
        validateConnectorCompatibility(vehicle, charger);
        validateNoOverlap(request.getChargerId(), request.getStartTime(), request.getEndTime());

        Reservation reservation = new Reservation();
        reservation.setVehicle(vehicle);
        reservation.setCharger(charger);
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setStatus(ReservationStatus.ACTIVE);
        return reservationRepository.save(reservation);
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

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
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
                List.of(ReservationStatus.ACTIVE),
                endTime,
                startTime
        );

        if (overlaps) {
            throw new BusinessException(HttpStatus.CONFLICT, "There is already a reservation for this charger in the selected time range.");
        }
    }
}
