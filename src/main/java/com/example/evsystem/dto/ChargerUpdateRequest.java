package com.example.evsystem.dto;

import com.example.evsystem.enums.ChargerStatus;
import com.example.evsystem.enums.ChargerType;
import com.example.evsystem.enums.ConnectorType;
import com.example.evsystem.enums.PowerOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public class ChargerUpdateRequest {

    @Schema(example = "CHG-1002")
    private String chargerCode;

    @Schema(example = "AC")
    private ChargerType chargerType;

    @Schema(example = "KW_50")
    private PowerOutput powerOutput;

    @Schema(example = "TYPE2")
    private ConnectorType connectorType;

    @Positive(message = "Price per kWh must be greater than zero.")
    @Schema(example = "9.50")
    private Float pricePerKwh;

    @Schema(example = "OCCUPIED")
    private ChargerStatus status;

    @Positive(message = "Station id must be greater than zero.")
    @Schema(example = "2")
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
