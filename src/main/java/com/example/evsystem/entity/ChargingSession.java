package com.example.evsystem.entity;

import com.example.evsystem.enums.ChargerStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "charging_sessions")
public class ChargingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    private Float startBatteryLevel;
    private Float endBatteryLevel;
    private Float consumedKwh = 0f;
    private Float totalCost = 0f;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    @Enumerated(EnumType.STRING)
    private ChargerStatus status = ChargerStatus.AVAILABLE;

    public ChargingSession() {}

    public Long getId()                 { return id; }
    public Reservation getReservation() { return reservation; }
    public Float getStartBatteryLevel() { return startBatteryLevel; }
    public Float getEndBatteryLevel()   { return endBatteryLevel; }
    public Float getConsumedKwh()       { return consumedKwh; }
    public Float getTotalCost()         { return totalCost; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getEndedAt()   { return endedAt; }
    public ChargerStatus getStatus()    { return status; }

    public void setReservation(Reservation reservation)       { this.reservation = reservation; }
    public void setStartBatteryLevel(Float startBatteryLevel) { this.startBatteryLevel = startBatteryLevel; }
    public void setEndBatteryLevel(Float endBatteryLevel)     { this.endBatteryLevel = endBatteryLevel; }
    public void setConsumedKwh(Float consumedKwh)             { this.consumedKwh = consumedKwh; }
    public void setTotalCost(Float totalCost)                 { this.totalCost = totalCost; }
    public void setStartedAt(LocalDateTime startedAt)         { this.startedAt = startedAt; }
    public void setEndedAt(LocalDateTime endedAt)             { this.endedAt = endedAt; }
    public void setStatus(ChargerStatus status)               { this.status = status; }
}