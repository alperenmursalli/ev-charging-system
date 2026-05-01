package com.example.evsystem.service;

import com.example.evsystem.dto.VehicleRequest;
import com.example.evsystem.dto.VehicleUpdateRequest;
import com.example.evsystem.entity.Vehicle;
import com.example.evsystem.exception.BusinessException;
import com.example.evsystem.repository.ChargingSessionRepository;
import com.example.evsystem.repository.ReservationRepository;
import com.example.evsystem.repository.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final ReservationRepository reservationRepository;
    private final ChargingSessionRepository chargingSessionRepository;
    private final CurrentUserService currentUserService;

    public VehicleService(VehicleRepository vehicleRepository,
                          ReservationRepository reservationRepository,
                          ChargingSessionRepository chargingSessionRepository,
                          CurrentUserService currentUserService) {
        this.vehicleRepository = vehicleRepository;
        this.reservationRepository = reservationRepository;
        this.chargingSessionRepository = chargingSessionRepository;
        this.currentUserService = currentUserService;
    }


    public Vehicle save(VehicleRequest request) {
        validateText(request.getBrand(), "Brand cannot be blank.");
        validateText(request.getModel(), "Model cannot be blank.");
        validateText(request.getPlateNumber(), "Plate number cannot be blank.");

        Vehicle vehicle = new Vehicle();
        vehicle.setBrand(request.getBrand().trim());
        vehicle.setModel(request.getModel().trim());
        vehicle.setBatteryCapacity(request.getBatteryCapacity());
        vehicle.setConnectorType(request.getConnectorType());
        vehicle.setPlateNumber(request.getPlateNumber().trim());
        currentUserService.getCurrentUser().ifPresent(vehicle::setOwner);

        if (vehicleRepository.existsByPlateNumberIgnoreCase(vehicle.getPlateNumber())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Vehicle with this plate number already exists.");
        }
        return vehicleRepository.save(vehicle);
    }

    @Transactional(readOnly = true)
    public List<Vehicle> getAllVehicles() {
        if (!currentUserService.isCurrentUserAdmin()) {
            return currentUserService.getCurrentUsername()
                    .map(vehicleRepository::findByOwnerUsernameIgnoreCase)
                    .orElseGet(vehicleRepository::findAll);
        }
        return vehicleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Vehicle getVehicleById(Long id) {
        if (!currentUserService.isCurrentUserAdmin() && currentUserService.getCurrentUsername().isPresent()) {
            return vehicleRepository.findByIdAndOwnerUsernameIgnoreCase(id, currentUserService.getCurrentUsername().get())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Vehicle not found with id: " + id));
        }

        return vehicleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Vehicle not found with id: " + id));
    }

    public Vehicle update(Long id, VehicleUpdateRequest request) {
        Vehicle vehicle = getVehicleById(id);

        if (request.getBrand() != null) {
            validateText(request.getBrand(), "Brand cannot be blank.");
            vehicle.setBrand(request.getBrand().trim());
        }
        if (request.getModel() != null) {
            validateText(request.getModel(), "Model cannot be blank.");
            vehicle.setModel(request.getModel().trim());
        }
        if (request.getBatteryCapacity() != null) {
            vehicle.setBatteryCapacity(request.getBatteryCapacity());
        }
        if (request.getConnectorType() != null) {
            vehicle.setConnectorType(request.getConnectorType());
        }
        if (request.getPlateNumber() != null) {
            validateText(request.getPlateNumber(), "Plate number cannot be blank.");
            String plateNumber = request.getPlateNumber().trim();
            if (vehicleRepository.existsByPlateNumberIgnoreCaseAndIdNot(plateNumber, id)) {
                throw new BusinessException(HttpStatus.CONFLICT, "Vehicle with this plate number already exists.");
            }
            vehicle.setPlateNumber(plateNumber);
        }

        return vehicleRepository.save(vehicle);
    }

    public void delete(Long id) {
        Vehicle vehicle = getVehicleById(id);
        if (reservationRepository.existsByVehicleId(id) || chargingSessionRepository.existsByReservation_Vehicle_Id(id)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Vehicle with reservation or charging session history cannot be deleted.");
        }
        vehicleRepository.delete(vehicle);
    }

    private void validateText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
