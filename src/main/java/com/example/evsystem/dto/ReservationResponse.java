package com.example.evsystem.dto;

import com.example.evsystem.entity.Reservation;
import com.example.evsystem.entity.ChargingSession;
import com.example.evsystem.enums.ChargingSessionStatus;
import com.example.evsystem.enums.ReservationStatus;

import java.time.LocalDateTime;

public class ReservationResponse {

    private Long id;
    private Long vehicleId;
    private Long chargerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ReservationStatus status;
    private Long sessionId;
    private ChargingSessionStatus sessionStatus;
    private Float consumedKwh;
    private Float totalCost;

    public static ReservationResponse from(Reservation reservation) {
        return from(reservation, null);
    }

    public static ReservationResponse from(Reservation reservation, ChargingSession session) {
        ReservationResponse response = new ReservationResponse();
        response.id = reservation.getId();
        response.vehicleId = reservation.getVehicle().getId();
        response.chargerId = reservation.getCharger().getId();
        response.startTime = reservation.getStartTime();
        response.endTime = reservation.getEndTime();
        response.status = reservation.getStatus();
        if (session != null) {
            response.sessionId = session.getId();
            response.sessionStatus = session.getStatus();
            response.consumedKwh = session.getConsumedKwh();
            response.totalCost = session.getTotalCost();
        }
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

    public Long getSessionId() {
        return sessionId;
    }

    public ChargingSessionStatus getSessionStatus() {
        return sessionStatus;
    }

    public Float getConsumedKwh() {
        return consumedKwh;
    }

    public Float getTotalCost() {
        return totalCost;
    }
}
