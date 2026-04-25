package com.example.evsystem.dto;

import com.example.evsystem.entity.Charger;
import com.example.evsystem.enums.ChargerStatus;
import com.example.evsystem.enums.ChargerType;
import com.example.evsystem.enums.ConnectorType;
import com.example.evsystem.enums.PowerOutput;

public class ChargerResponse {

    private Long id;
    private String chargerCode;
    private ChargerType chargerType;
    private PowerOutput powerOutput;
    private ConnectorType connectorType;
    private Float pricePerKwh;
    private ChargerStatus status;
    private Long stationId;

    public static ChargerResponse from(Charger charger) {
        ChargerResponse response = new ChargerResponse();
        response.id = charger.getId();
        response.chargerCode = charger.getChargerCode();
        response.chargerType = charger.getChargerType();
        response.powerOutput = charger.getPowerOutput();
        response.connectorType = charger.getConnectorType();
        response.pricePerKwh = charger.getPricePerKwh();
        response.status = charger.getStatus();
        response.stationId = charger.getStation() != null ? charger.getStation().getId() : null;
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getChargerCode() {
        return chargerCode;
    }

    public ChargerType getChargerType() {
        return chargerType;
    }

    public PowerOutput getPowerOutput() {
        return powerOutput;
    }

    public ConnectorType getConnectorType() {
        return connectorType;
    }

    public Float getPricePerKwh() {
        return pricePerKwh;
    }

    public ChargerStatus getStatus() {
        return status;
    }

    public Long getStationId() {
        return stationId;
    }
}
