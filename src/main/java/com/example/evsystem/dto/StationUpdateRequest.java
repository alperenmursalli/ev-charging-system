package com.example.evsystem.dto;

import com.example.evsystem.enums.StationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public class StationUpdateRequest {

    @Schema(example = "Besiktas Supercharge")
    private String name;

    @Schema(example = "Sinanpasa, Besiktas / Istanbul")
    private String address;

    @Schema(example = "41.0438")
    private Double latitude;

    @Schema(example = "29.0094")
    private Double longitude;

    @Schema(example = "OCCUPIED")
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
