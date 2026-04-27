package com.example.evsystem.dto;

import com.example.evsystem.enums.ConnectorType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class VehicleRequest {

    @NotBlank(message = "Brand cannot be blank.")
    @Schema(example = "Tesla")
    private String brand;

    @NotBlank(message = "Model cannot be blank.")
    @Schema(example = "Model Y")
    private String model;

    @NotNull(message = "Battery capacity is required.")
    @Positive(message = "Battery capacity must be greater than zero.")
    @Schema(example = "75.0")
    private Double batteryCapacity;

    @NotNull(message = "Connector type is required.")
    @Schema(example = "CCS")
    private ConnectorType connectorType;

    @NotBlank(message = "Plate number cannot be blank.")
    @Schema(example = "34ABC123")
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
