package com.example.evsystem.dto;

import com.example.evsystem.entity.Station;
import com.example.evsystem.enums.StationStatus;

public class StationResponse {

    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private StationStatus status;

    public static StationResponse from(Station station) {
        StationResponse response = new StationResponse();
        response.id = station.getId();
        response.name = station.getName();
        response.address = station.getAddress();
        response.latitude = station.getLatitude();
        response.longitude = station.getLongitude();
        response.status = station.getStatus();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public StationStatus getStatus() {
        return status;
    }
}
