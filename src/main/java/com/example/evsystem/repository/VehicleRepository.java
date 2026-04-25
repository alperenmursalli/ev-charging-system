package com.example.evsystem.repository;

import com.example.evsystem.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    boolean existsByPlateNumberIgnoreCase(String plateNumber);
}
