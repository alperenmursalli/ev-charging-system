package com.example.evsystem.entity;
import com.example.evsystem.enums.ConnectorType;
import jakarta.persistence.*;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;
    private String model;
    private Double batteryCapacity;
    @Enumerated(EnumType.STRING)
    private ConnectorType connectorType;
    private String plateNumber;

    public Long getId() { return id; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Double getBatteryCapacity() { return batteryCapacity; }
    public void setBatteryCapacity(Double batteryCapacity) { this.batteryCapacity = batteryCapacity; }

    public ConnectorType getConnectorType() { return connectorType; }
    public void setConnectorType(ConnectorType connectorType) { this.connectorType = connectorType; }

    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
}