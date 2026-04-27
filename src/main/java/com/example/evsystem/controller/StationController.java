package com.example.evsystem.controller;

import com.example.evsystem.dto.ChargerResponse;
import com.example.evsystem.dto.StationRequest;
import com.example.evsystem.dto.StationDiscoveryResponse;
import com.example.evsystem.dto.StationResponse;
import com.example.evsystem.dto.StationUpdateRequest;
import com.example.evsystem.entity.Charger;
import com.example.evsystem.enums.ConnectorType;
import com.example.evsystem.enums.PowerOutput;
import com.example.evsystem.enums.StationStatus;
import com.example.evsystem.service.ChargerService;
import com.example.evsystem.service.StationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stations")
@Validated
@Tag(name = "Stations", description = "Station management and discovery endpoints")
public class StationController {

    private final StationService stationService;
    private final ChargerService chargerService;

    public StationController(StationService stationService, ChargerService chargerService) {
        this.stationService = stationService;
        this.chargerService = chargerService;
    }

    @Operation(summary = "Create station", description = "Creates a new charging station.")
    @PostMapping
    public ResponseEntity<StationResponse> createStation(@Valid @RequestBody StationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(StationResponse.from(stationService.save(request)));
    }

    @Operation(summary = "List stations", description = "Returns all stations.")
    @GetMapping
    public ResponseEntity<List<StationResponse>> getAllStations() {
        return ResponseEntity.ok(stationService.getAll().stream()
                .map(StationResponse::from)
                .toList());
    }

    @Operation(summary = "Discover stations", description = "Returns stations filtered by connector type, power, price, status, and optional user location.")
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

    @Operation(summary = "Get station by id", description = "Returns one station by its id.")
    @GetMapping("/{id}")
    public ResponseEntity<StationResponse> getStationById(@PathVariable @Positive(message = "Station id must be greater than zero.") Long id) {
        return ResponseEntity.ok(StationResponse.from(stationService.getById(id)));
    }

    @Operation(summary = "List station chargers", description = "Returns all chargers belonging to a station.")
    @GetMapping("/{id}/chargers")
    public ResponseEntity<List<ChargerResponse>> getChargersByStation(@PathVariable @Positive(message = "Station id must be greater than zero.") Long id) {
        List<Charger> chargers = chargerService.getByStationId(id);
        return ResponseEntity.ok(chargers.stream()
                .map(ChargerResponse::from)
                .toList());
    }

    @Operation(summary = "Update station", description = "Updates the provided station fields.")
    @PutMapping("/{id}")
    public ResponseEntity<StationResponse> updateStation(@PathVariable @Positive(message = "Station id must be greater than zero.") Long id, @Valid @RequestBody StationUpdateRequest request) {
        return ResponseEntity.ok(StationResponse.from(stationService.update(id, request)));
    }

    @Operation(summary = "Delete station", description = "Deletes a station by id.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStation(@PathVariable @Positive(message = "Station id must be greater than zero.") Long id) {
        stationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
