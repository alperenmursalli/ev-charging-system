package com.example.evsystem.dto;

import com.example.evsystem.entity.Reservation;
import com.example.evsystem.enums.ReservationStatus;

import java.time.LocalDateTime;

public class ReservationResponse {

    private Long id;
    private Long vehicleId;
    private Long chargerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ReservationStatus status;

    public static ReservationResponse from(Reservation reservation) {
        ReservationResponse response = new ReservationResponse();
        response.id = reservation.getId();
        response.vehicleId = reservation.getVehicle().getId();
        response.chargerId = reservation.getCharger().getId();
        response.startTime = reservation.getStartTime();
        response.endTime = reservation.getEndTime();
        response.status = reservation.getStatus();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public Long getChargerId() {
        return chargerId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}
