package com.example.evsystem.controller;

import com.example.evsystem.dto.VehicleRequest;
import com.example.evsystem.dto.VehicleResponse;
import com.example.evsystem.dto.VehicleUpdateRequest;
import com.example.evsystem.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vehicles")
@Validated
@Tag(name = "Vehicles", description = "Vehicle management endpoints")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @Operation(summary = "Create vehicle", description = "Creates a new vehicle record.")
    @PostMapping
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(VehicleResponse.from(vehicleService.save(request)));
    }

    @Operation(summary = "List vehicles", description = "Returns all vehicles.")
    @GetMapping
    public List<VehicleResponse> getAllVehicles() {
        return vehicleService.getAllVehicles().stream()
                .map(VehicleResponse::from)
                .toList();
    }

    @Operation(summary = "Get vehicle by id", description = "Returns one vehicle by its id.")
    @GetMapping("/{id}")
    public VehicleResponse getVehicleById(@PathVariable @Positive(message = "Vehicle id must be greater than zero.") Long id) {
        return VehicleResponse.from(vehicleService.getVehicleById(id));
    }

    @Operation(summary = "Update vehicle", description = "Updates the provided vehicle fields.")
    @PutMapping("/{id}")
    public VehicleResponse updateVehicle(@PathVariable @Positive(message = "Vehicle id must be greater than zero.") Long id, @Valid @RequestBody VehicleUpdateRequest request) {
        return VehicleResponse.from(vehicleService.update(id, request));
    }

    @Operation(summary = "Delete vehicle", description = "Deletes a vehicle by id.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable @Positive(message = "Vehicle id must be greater than zero.") Long id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
