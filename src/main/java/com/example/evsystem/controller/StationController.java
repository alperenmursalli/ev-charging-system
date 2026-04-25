package com.example.evsystem.controller;

import com.example.evsystem.entity.Charger;
import com.example.evsystem.entity.Station;
import com.example.evsystem.service.ChargerService;
import com.example.evsystem.service.StationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stations")
public class StationController {

    private final StationService stationService;
    private final ChargerService chargerService;

    public StationController(StationService stationService, ChargerService chargerService) {
        this.stationService = stationService;
        this.chargerService = chargerService;
    }

    @PostMapping
    public ResponseEntity<Station> createStation(@Valid @RequestBody Station station) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stationService.save(station));
    }

    @GetMapping
    public ResponseEntity<List<Station>> getAllStations() {
        return ResponseEntity.ok(stationService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Station> getStationById(@PathVariable Long id) {
        return ResponseEntity.ok(stationService.getById(id));
    }

    @GetMapping("/{id}/chargers")
    public ResponseEntity<List<Charger>> getChargersByStation(@PathVariable Long id) {
        return ResponseEntity.ok(chargerService.getByStationId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Station> updateStation(@PathVariable Long id, @Valid @RequestBody Station station) {
        return ResponseEntity.ok(stationService.update(id, station));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStation(@PathVariable Long id) {
        stationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}