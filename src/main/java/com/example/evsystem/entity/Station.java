package com.example.evsystem.entity;

import com.example.evsystem.enums.ChargerStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "stations")
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    private ChargerStatus status;

    @JsonManagedReference
    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL)
    private List<Charger> chargers;

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public ChargerStatus getStatus() { return status; }
    public void setStatus(ChargerStatus status) { this.status = status; }

    public List<Charger> getChargers() { return chargers; }
    public void setChargers(List<Charger> chargers) { this.chargers = chargers; }
}