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
    private Float chargedPercentage;
    private Float consumedKwh;
    private Float startEnergyKwh;
    private Float endEnergyKwh;
    private Float unitPricePerKwh;
    private Float totalCost;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime reservationStartTime;
    private LocalDateTime reservationEndTime;
    private ChargingSessionStatus status;

    public static ChargingSessionResponse from(ChargingSession session) {
        ChargingSessionResponse response = new ChargingSessionResponse();
        response.id = session.getId();
        response.reservationId = session.getReservation().getId();
        response.chargerId = session.getReservation().getCharger().getId();
        response.startBatteryLevel = session.getStartBatteryLevel();
        response.endBatteryLevel = session.getEndBatteryLevel();
        Double batteryCapacity = session.getReservation().getVehicle().getBatteryCapacity();
        if (session.getEndBatteryLevel() != null && session.getStartBatteryLevel() != null) {
            response.chargedPercentage = session.getEndBatteryLevel() - session.getStartBatteryLevel();
        }
        if (batteryCapacity != null) {
            if (session.getStartBatteryLevel() != null) {
                response.startEnergyKwh = round((float) (batteryCapacity * session.getStartBatteryLevel() / 100d));
            }
            if (session.getEndBatteryLevel() != null) {
                response.endEnergyKwh = round((float) (batteryCapacity * session.getEndBatteryLevel() / 100d));
            }
        }
        response.consumedKwh = session.getConsumedKwh();
        response.unitPricePerKwh = session.getReservation().getCharger().getPricePerKwh();
        response.totalCost = session.getTotalCost();
        response.startedAt = session.getStartedAt();
        response.endedAt = session.getEndedAt();
        response.reservationStartTime = session.getReservation().getStartTime();
        response.reservationEndTime = session.getReservation().getEndTime();
        response.status = session.getStatus();
        return response;
    }

    private static Float round(float value) {
        return Math.round(value * 100f) / 100f;
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

    public Float getChargedPercentage() {
        return chargedPercentage;
    }

    public Float getConsumedKwh() {
        return consumedKwh;
    }

    public Float getStartEnergyKwh() {
        return startEnergyKwh;
    }

    public Float getEndEnergyKwh() {
        return endEnergyKwh;
    }

    public Float getUnitPricePerKwh() {
        return unitPricePerKwh;
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

    public LocalDateTime getReservationStartTime() {
        return reservationStartTime;
    }

    public LocalDateTime getReservationEndTime() {
        return reservationEndTime;
    }

    public ChargingSessionStatus getStatus() {
        return status;
    }
}
