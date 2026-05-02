package com.example.evsystem.service;

import com.example.evsystem.dto.VehicleRequest;
import com.example.evsystem.entity.AppUser;
import com.example.evsystem.entity.Vehicle;
import com.example.evsystem.enums.ConnectorType;
import com.example.evsystem.enums.UserRole;
import com.example.evsystem.exception.BusinessException;
import com.example.evsystem.repository.ChargingSessionRepository;
import com.example.evsystem.repository.ReservationRepository;
import com.example.evsystem.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VehicleServiceTest {

    private VehicleRepository vehicleRepository;
    private ReservationRepository reservationRepository;
    private ChargingSessionRepository chargingSessionRepository;
    private CurrentUserService currentUserService;
    private VehicleService vehicleService;

    @BeforeEach
    void setUp() {
        vehicleRepository = mock(VehicleRepository.class);
        reservationRepository = mock(ReservationRepository.class);
        chargingSessionRepository = mock(ChargingSessionRepository.class);
        currentUserService = mock(CurrentUserService.class);
        vehicleService = new VehicleService(vehicleRepository, reservationRepository, chargingSessionRepository, currentUserService);
    }

    @Test
    void saveAssignsCurrentUserAsOwner() {
        AppUser user = user("alice", UserRole.ROLE_USER);
        when(currentUserService.getCurrentUser()).thenReturn(Optional.of(user));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vehicle saved = vehicleService.save(vehicleRequest());

        assertSame(user, saved.getOwner());
    }

    @Test
    void listFiltersVehiclesForRegularUser() {
        when(currentUserService.isCurrentUserAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUsername()).thenReturn(Optional.of("alice"));
        when(vehicleRepository.findByOwnerUsernameIgnoreCase("alice")).thenReturn(List.of(new Vehicle()));

        List<Vehicle> vehicles = vehicleService.getAllVehicles();

        assertEquals(1, vehicles.size());
        verify(vehicleRepository).findByOwnerUsernameIgnoreCase("alice");
        verify(vehicleRepository, never()).findAll();
    }

    @Test
    void deleteRejectsVehicleWithReservationHistory() {
        Vehicle vehicle = new Vehicle();
        when(currentUserService.isCurrentUserAdmin()).thenReturn(true);
        when(vehicleRepository.findById(5L)).thenReturn(Optional.of(vehicle));
        when(reservationRepository.existsByVehicleId(5L)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> vehicleService.delete(5L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(vehicleRepository, never()).delete(any());
    }

    private VehicleRequest vehicleRequest() {
        VehicleRequest request = new VehicleRequest();
        request.setBrand("Tesla");
        request.setModel("Model 3");
        request.setBatteryCapacity(75.0);
        request.setConnectorType(ConnectorType.CCS);
        request.setPlateNumber("34ABC123");
        return request;
    }

    private AppUser user(String username, UserRole role) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash("hash");
        user.setRole(role);
        user.setEnabled(true);
        return user;
    }
}
