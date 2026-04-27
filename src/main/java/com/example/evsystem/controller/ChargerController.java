package com.example.evsystem.controller;

import com.example.evsystem.dto.ChargerRequest;
import com.example.evsystem.dto.ChargerResponse;
import com.example.evsystem.dto.ChargerUpdateRequest;
import com.example.evsystem.service.ChargerService;
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
@RequestMapping("/chargers")
@Validated
@Tag(name = "Chargers", description = "Charger management endpoints")
public class ChargerController {

    private final ChargerService chargerService;

    public ChargerController(ChargerService chargerService) {
        this.chargerService = chargerService;
    }

    @Operation(summary = "Create charger", description = "Creates a new charger and links it to a station.")
    @PostMapping
    public ResponseEntity<ChargerResponse> createCharger(@Valid @RequestBody ChargerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ChargerResponse.from(chargerService.save(request)));
    }

    @Operation(summary = "List chargers", description = "Returns all chargers.")
    @GetMapping
    public ResponseEntity<List<ChargerResponse>> getAllChargers() {
        return ResponseEntity.ok(chargerService.getAll().stream()
                .map(ChargerResponse::from)
                .toList());
    }

    @Operation(summary = "Get charger by id", description = "Returns one charger by its id.")
    @GetMapping("/{id}")
    public ResponseEntity<ChargerResponse> getChargerById(@PathVariable @Positive(message = "Charger id must be greater than zero.") Long id) {
        return ResponseEntity.ok(ChargerResponse.from(chargerService.getById(id)));
    }

    @Operation(summary = "Update charger", description = "Updates the provided charger fields.")
    @PutMapping("/{id}")
    public ResponseEntity<ChargerResponse> updateCharger(@PathVariable @Positive(message = "Charger id must be greater than zero.") Long id, @Valid @RequestBody ChargerUpdateRequest request) {
        return ResponseEntity.ok(ChargerResponse.from(chargerService.update(id, request)));
    }

    @Operation(summary = "Delete charger", description = "Deletes a charger by id.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCharger(@PathVariable @Positive(message = "Charger id must be greater than zero.") Long id) {
        chargerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
