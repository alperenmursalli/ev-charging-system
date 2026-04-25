package com.example.evsystem.controller;

import com.example.evsystem.dto.VehicleResponse;
import com.example.evsystem.entity.Vehicle;
import com.example.evsystem.service.VehicleService;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public VehicleResponse createVehicle(@Valid @RequestBody Vehicle vehicle) {
        return VehicleResponse.from(vehicleService.save(vehicle));
    }

    @GetMapping
    public List<VehicleResponse> getAllVehicles() {
        return vehicleService.getAllVehicles().stream()
                .map(VehicleResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public VehicleResponse getVehicleById(@PathVariable Long id) {
        return VehicleResponse.from(vehicleService.getVehicleById(id));
    }
}
