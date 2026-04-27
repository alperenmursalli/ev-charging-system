package com.example.evsystem.dto;

import com.example.evsystem.validation.ValidReservationTime;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

@ValidReservationTime
public class CreateReservationRequest {

    @NotNull(message = "Vehicle id is required.")
    @Positive(message = "Vehicle id must be greater than zero.")
    @Schema(example = "1")
    private Long vehicleId;

    @NotNull(message = "Charger id is required.")
    @Positive(message = "Charger id must be greater than zero.")
    @Schema(example = "10")
    private Long chargerId;

    @NotNull(message = "Reservation start time is required.")
    @Schema(example = "2026-04-28T10:00:00")
    private LocalDateTime startTime;

    @NotNull(message = "Reservation end time is required.")
    @Schema(example = "2026-04-28T11:30:00")
    private LocalDateTime endTime;

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Long getChargerId() {
        return chargerId;
    }

    public void setChargerId(Long chargerId) {
        this.chargerId = chargerId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
