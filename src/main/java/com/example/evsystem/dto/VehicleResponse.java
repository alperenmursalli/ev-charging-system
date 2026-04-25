package com.example.evsystem.dto;

import com.example.evsystem.entity.Vehicle;
import com.example.evsystem.enums.ConnectorType;

public class VehicleResponse {

    private Long id;
    private String brand;
    private String model;
    private Double batteryCapacity;
    private ConnectorType connectorType;
    private String plateNumber;

    public static VehicleResponse from(Vehicle vehicle) {
        VehicleResponse response = new VehicleResponse();
        response.id = vehicle.getId();
        response.brand = vehicle.getBrand();
        response.model = vehicle.getModel();
        response.batteryCapacity = vehicle.getBatteryCapacity();
        response.connectorType = vehicle.getConnectorType();
        response.plateNumber = vehicle.getPlateNumber();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public Double getBatteryCapacity() {
        return batteryCapacity;
    }

    public ConnectorType getConnectorType() {
        return connectorType;
    }

    public String getPlateNumber() {
        return plateNumber;
    }
}
