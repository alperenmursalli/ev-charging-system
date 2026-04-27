package com.example.evsystem.dto;

import com.example.evsystem.enums.ConnectorType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public class VehicleUpdateRequest {

    @Schema(example = "Tesla")
    private String brand;

    @Schema(example = "Model 3")
    private String model;

    @Positive(message = "Battery capacity must be greater than zero.")
    @Schema(example = "68.0")
    private Double batteryCapacity;

    @Schema(example = "CCS")
    private ConnectorType connectorType;

    @Schema(example = "34XYZ987")
    private String plateNumber;

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Double getBatteryCapacity() {
        return batteryCapacity;
    }

    public void setBatteryCapacity(Double batteryCapacity) {
        this.batteryCapacity = batteryCapacity;
    }

    public ConnectorType getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(ConnectorType connectorType) {
        this.connectorType = connectorType;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }
}
