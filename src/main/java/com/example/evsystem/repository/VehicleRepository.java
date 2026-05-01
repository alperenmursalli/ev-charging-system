package com.example.evsystem.repository;

import com.example.evsystem.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    boolean existsByPlateNumberIgnoreCase(String plateNumber);
    boolean existsByPlateNumberIgnoreCaseAndIdNot(String plateNumber, Long id);
    List<Vehicle> findByOwnerUsernameIgnoreCase(String username);
    Optional<Vehicle> findByIdAndOwnerUsernameIgnoreCase(Long id, String username);
}
