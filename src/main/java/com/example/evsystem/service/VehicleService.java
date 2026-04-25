package com.example.evsystem.service;

import com.example.evsystem.entity.Vehicle;
import com.example.evsystem.exception.BusinessException;
import com.example.evsystem.repository.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }


    public Vehicle save(Vehicle vehicle) {
        if (vehicleRepository.existsByPlateNumberIgnoreCase(vehicle.getPlateNumber())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Vehicle with this plate number already exists.");
        }
        return vehicleRepository.save(vehicle);
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public Vehicle getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Vehicle not found with id: " + id));
    }
}
