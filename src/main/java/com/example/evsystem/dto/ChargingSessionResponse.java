package com.example.evsystem.dto;

import com.example.evsystem.entity.ChargingSession;
import com.example.evsystem.enums.ChargingSessionStatus;

import java.time.LocalDateTime;

public class ChargingSessionResponse {

    private Long id;
    private Long reservationId;
    private Long chargerId;
    private Float startBatteryLevel;
    private Float endBatteryLevel;
    private Float consumedKwh;
    private Float totalCost;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private ChargingSessionStatus status;

    public static ChargingSessionResponse from(ChargingSession session) {
        ChargingSessionResponse response = new ChargingSessionResponse();
        response.id = session.getId();
        response.reservationId = session.getReservation().getId();
        response.chargerId = session.getReservation().getCharger().getId();
        response.startBatteryLevel = session.getStartBatteryLevel();
        response.endBatteryLevel = session.getEndBatteryLevel();
        response.consumedKwh = session.getConsumedKwh();
        response.totalCost = session.getTotalCost();
        response.startedAt = session.getStartedAt();
        response.endedAt = session.getEndedAt();
        response.status = session.getStatus();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public Long getChargerId() {
        return chargerId;
    }

    public Float getStartBatteryLevel() {
        return startBatteryLevel;
    }

    public Float getEndBatteryLevel() {
        return endBatteryLevel;
    }

    public Float getConsumedKwh() {
        return consumedKwh;
    }

    public Float getTotalCost() {
        return totalCost;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public ChargingSessionStatus getStatus() {
        return status;
    }
}
