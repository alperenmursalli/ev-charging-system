package com.example.evsystem.service;

import com.example.evsystem.entity.Vehicle;
import com.example.evsystem.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }


    public Vehicle save(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    // GÖREV: Araç Listeleme
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    // GÖREV: Araç Detayını Getirme
    public Vehicle getVehicleById(Long id) {
        // Eğer ID yoksa hata fırlatıyoruz (İleride özel hata sınıfları yazabilirsiniz)
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Araç bulunamadı! ID: " + id));
    }
}