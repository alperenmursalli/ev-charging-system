package com.example.evsystem.dto;

import com.example.evsystem.enums.ChargerStatus;
import com.example.evsystem.enums.ChargerType;
import com.example.evsystem.enums.ConnectorType;
import com.example.evsystem.enums.PowerOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ChargerRequest {

    @NotBlank(message = "Charger code cannot be blank.")
    @Schema(example = "CHG-1001")
    private String chargerCode;

    @NotNull(message = "Charger type is required.")
    @Schema(example = "DC")
    private ChargerType chargerType;

    @NotNull(message = "Power output is required.")
    @Schema(example = "KW_150")
    private PowerOutput powerOutput;

    @NotNull(message = "Connector type is required.")
    @Schema(example = "CCS")
    private ConnectorType connectorType;

    @NotNull(message = "Price per kWh is required.")
    @Positive(message = "Price per kWh must be greater than zero.")
    @Schema(example = "11.99")
    private Float pricePerKwh;

    @NotNull(message = "Charger status is required.")
    @Schema(example = "AVAILABLE")
    private ChargerStatus status;

    @NotNull(message = "Station id is required.")
    @Positive(message = "Station id must be greater than zero.")
    @Schema(example = "1")
    private Long stationId;

    public String getChargerCode() {
        return chargerCode;
    }

    public void setChargerCode(String chargerCode) {
        this.chargerCode = chargerCode;
    }

    public ChargerType getChargerType() {
        return chargerType;
    }

    public void setChargerType(ChargerType chargerType) {
        this.chargerType = chargerType;
    }

    public PowerOutput getPowerOutput() {
        return powerOutput;
    }

    public void setPowerOutput(PowerOutput powerOutput) {
        this.powerOutput = powerOutput;
    }

    public ConnectorType getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(ConnectorType connectorType) {
        this.connectorType = connectorType;
    }

    public Float getPricePerKwh() {
        return pricePerKwh;
    }

    public void setPricePerKwh(Float pricePerKwh) {
        this.pricePerKwh = pricePerKwh;
    }

    public ChargerStatus getStatus() {
        return status;
    }

    public void setStatus(ChargerStatus status) {
        this.status = status;
    }

    public Long getStationId() {
        return stationId;
    }

    public void setStationId(Long stationId) {
        this.stationId = stationId;
    }
}
