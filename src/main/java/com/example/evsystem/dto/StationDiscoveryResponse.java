package com.example.evsystem.dto;

import com.example.evsystem.entity.Charger;
import com.example.evsystem.entity.Station;
import com.example.evsystem.enums.ChargerStatus;
import com.example.evsystem.enums.ConnectorType;
import com.example.evsystem.enums.PowerOutput;
import com.example.evsystem.enums.StationStatus;

import java.util.Comparator;
import java.util.List;

public class StationDiscoveryResponse {

    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private StationStatus status;
    private Integer totalChargerCount;
    private Integer availableChargerCount;
    private Float minPricePerKwh;
    private List<ConnectorType> connectorTypes;
    private List<PowerOutput> powerOutputs;
    private Double distanceKm;

    public static StationDiscoveryResponse from(Station station, Double userLatitude, Double userLongitude) {
        StationDiscoveryResponse response = new StationDiscoveryResponse();
        List<Charger> chargers = station.getChargers() == null ? List.of() : station.getChargers();

        response.id = station.getId();
        response.name = station.getName();
        response.address = station.getAddress();
        response.latitude = station.getLatitude();
        response.longitude = station.getLongitude();
        response.status = deriveStatus(station, chargers);
        response.totalChargerCount = chargers.size();
        response.availableChargerCount = (int) chargers.stream()
                .filter(charger -> charger.getStatus() == ChargerStatus.AVAILABLE)
                .count();
        response.minPricePerKwh = chargers.stream()
                .map(Charger::getPricePerKwh)
                .min(Comparator.naturalOrder())
                .orElse(null);
        response.connectorTypes = chargers.stream()
                .map(Charger::getConnectorType)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        response.powerOutputs = chargers.stream()
                .map(Charger::getPowerOutput)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        if (userLatitude != null && userLongitude != null) {
            response.distanceKm = haversineKm(userLatitude, userLongitude, station.getLatitude(), station.getLongitude());
        }

        return response;
    }

    private static StationStatus deriveStatus(Station station, List<Charger> chargers) {
        if (station.getStatus() == StationStatus.OFFLINE) {
            return StationStatus.OFFLINE;
        }
        if (chargers.stream().anyMatch(charger -> charger.getStatus() == ChargerStatus.AVAILABLE)) {
            return StationStatus.AVAILABLE;
        }
        if (chargers.stream().anyMatch(charger -> charger.getStatus() == ChargerStatus.OCCUPIED)) {
            return StationStatus.OCCUPIED;
        }
        return station.getStatus();
    }

    private static double haversineKm(double startLat, double startLon, double endLat, double endLon) {
        double earthRadiusKm = 6371.0;
        double latDistance = Math.toRadians(endLat - startLat);
        double lonDistance = Math.toRadians(endLon - startLon);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(startLat)) * Math.cos(Math.toRadians(endLat))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(earthRadiusKm * c * 100.0) / 100.0;
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

    public Integer getTotalChargerCount() {
        return totalChargerCount;
    }

    public Integer getAvailableChargerCount() {
        return availableChargerCount;
    }

    public Float getMinPricePerKwh() {
        return minPricePerKwh;
    }

    public List<ConnectorType> getConnectorTypes() {
        return connectorTypes;
    }

    public List<PowerOutput> getPowerOutputs() {
        return powerOutputs;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }
}
