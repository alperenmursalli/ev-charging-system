package com.example.evsystem.entity;
import com.example.evsystem.enums.ConnectorType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Can't be blank")
    private String brand;

    @NotBlank(message = "Can't be blank")
    private String model;

    @Positive(message = "Battery must be bigger than 0")
    private Double batteryCapacity;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "A connector type must be chosen")
    private ConnectorType connectorType;

    @NotBlank(message = "Plate can't be null")
    @Column(unique = true)
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