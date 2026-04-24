package com.example.evsystem.dto;

import com.example.evsystem.entity.Reservation;
import com.example.evsystem.enums.ReservationStatus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        response.chargerId = readEntityId(reservation.getCharger());
        response.startTime = reservation.getStartTime();
        response.endTime = reservation.getEndTime();
        response.status = reservation.getStatus();
        return response;
    }

    private static Long readEntityId(Object entity) {
        try {
            Method method = entity.getClass().getMethod("getId");
            Object value = method.invoke(entity);
            if (value instanceof Long id) {
                return id;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            // Reservation can still be created even if the related module is merged later.
        }

        return null;
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
