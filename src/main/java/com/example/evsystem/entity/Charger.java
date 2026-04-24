package com.example.evsystem.entity;

import com.example.evsystem.enums.ChargerStatus;
import com.example.evsystem.enums.ChargerType;
import com.example.evsystem.enums.ConnectorType;
import com.example.evsystem.enums.PowerOutput;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "chargers")
public class Charger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chargerCode;

    @NotNull(message = "Charger type cannot be null")
    @Enumerated(EnumType.STRING)
    private ChargerType chargerType;

    @NotNull(message = "Power output cannot be null")
    @Enumerated(EnumType.STRING)
    private PowerOutput powerOutput;

    @NotNull(message = "Connector type cannot be null")
    @Enumerated(EnumType.STRING)
    private ConnectorType connectorType;

    @NotNull(message = "Price per kWh cannot be null")
    @Positive(message = "Price per kWh must be positive")
    private Float pricePerKwh;

    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    private ChargerStatus status;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "station_id")
    private Station station;

    public Long getId() { return id; }

    public String getChargerCode() { return chargerCode; }
    public void setChargerCode(String chargerCode) { this.chargerCode = chargerCode; }

    public ChargerType getChargerType() { return chargerType; }
    public void setChargerType(ChargerType chargerType) { this.chargerType = chargerType; }

    public PowerOutput getPowerOutput() { return powerOutput; }
    public void setPowerOutput(PowerOutput powerOutput) { this.powerOutput = powerOutput; }

    public ConnectorType getConnectorType() { return connectorType; }
    public void setConnectorType(ConnectorType connectorType) { this.connectorType = connectorType; }

    public Float getPricePerKwh() { return pricePerKwh; }
    public void setPricePerKwh(Float pricePerKwh) { this.pricePerKwh = pricePerKwh; }

    public ChargerStatus getStatus() { return status; }
    public void setStatus(ChargerStatus status) { this.status = status; }

    public Station getStation() { return station; }
    public void setStation(Station station) { this.station = station; }
}