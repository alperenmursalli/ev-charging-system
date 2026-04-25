package com.example.evsystem.controller;

import com.example.evsystem.dto.ChargerResponse;
import com.example.evsystem.dto.StationDiscoveryResponse;
import com.example.evsystem.dto.StationResponse;
import com.example.evsystem.entity.Charger;
import com.example.evsystem.entity.Station;
import com.example.evsystem.enums.ConnectorType;
import com.example.evsystem.enums.PowerOutput;
import com.example.evsystem.enums.StationStatus;
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
    public ResponseEntity<StationResponse> createStation(@Valid @RequestBody Station station) {
        return ResponseEntity.status(HttpStatus.CREATED).body(StationResponse.from(stationService.save(station)));
    }

    @GetMapping
    public ResponseEntity<List<StationResponse>> getAllStations() {
        return ResponseEntity.ok(stationService.getAll().stream()
                .map(StationResponse::from)
                .toList());
    }

    @GetMapping("/discovery")
    public ResponseEntity<List<StationDiscoveryResponse>> discoverStations(
            @RequestParam(required = false) ConnectorType connectorType,
            @RequestParam(required = false) PowerOutput powerOutput,
            @RequestParam(required = false) Float maxPricePerKwh,
            @RequestParam(required = false) StationStatus status,
            @RequestParam(required = false) Double userLatitude,
            @RequestParam(required = false) Double userLongitude
    ) {
        return ResponseEntity.ok(stationService.discover(
                connectorType,
                powerOutput,
                maxPricePerKwh,
                status,
                userLatitude,
                userLongitude
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StationResponse> getStationById(@PathVariable Long id) {
        return ResponseEntity.ok(StationResponse.from(stationService.getById(id)));
    }

    @GetMapping("/{id}/chargers")
    public ResponseEntity<List<ChargerResponse>> getChargersByStation(@PathVariable Long id) {
        List<Charger> chargers = chargerService.getByStationId(id);
        return ResponseEntity.ok(chargers.stream()
                .map(ChargerResponse::from)
                .toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<StationResponse> updateStation(@PathVariable Long id, @Valid @RequestBody Station station) {
        return ResponseEntity.ok(StationResponse.from(stationService.update(id, station)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStation(@PathVariable Long id) {
        stationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
