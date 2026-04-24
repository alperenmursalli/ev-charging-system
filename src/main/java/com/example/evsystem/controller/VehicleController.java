package com.example.evsystem.controller;

import com.example.evsystem.entity.Vehicle;
import com.example.evsystem.service.VehicleService;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    // GÖREV: Araç Ekleme Endpoint'i
    @PostMapping
    public Vehicle createVehicle(@Valid @RequestBody Vehicle vehicle) {
        return vehicleService.save(vehicle);
    }

    // GÖREV: Araç Listeleme Endpoint'i
    @GetMapping
    public List<Vehicle> getAllVehicles() {
        return vehicleService.getAllVehicles();
    }

    // GÖREV: Araç Detayını Getirme Endpoint'i
    @GetMapping("/{id}")
    public Vehicle getVehicleById(@PathVariable Long id) {
        return vehicleService.getVehicleById(id);
    }
}
