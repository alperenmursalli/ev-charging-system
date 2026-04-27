package com.example.evsystem.dto;

import com.example.evsystem.enums.StationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StationRequest {

    @NotBlank(message = "Station name cannot be blank.")
    @Schema(example = "Kadikoy Fast Charge")
    private String name;

    @NotBlank(message = "Address cannot be blank.")
    @Schema(example = "Caferaga, Kadikoy / Istanbul")
    private String address;

    @NotNull(message = "Latitude is required.")
    @Schema(example = "40.9909")
    private Double latitude;

    @NotNull(message = "Longitude is required.")
    @Schema(example = "29.0277")
    private Double longitude;

    @NotNull(message = "Station status is required.")
    @Schema(example = "AVAILABLE")
    private StationStatus status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public StationStatus getStatus() {
        return status;
    }

    public void setStatus(StationStatus status) {
        this.status = status;
    }
}
