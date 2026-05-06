package com.example.evsystem.service;

import com.example.evsystem.dto.CreateReservationRequest;
import com.example.evsystem.entity.AppUser;
import com.example.evsystem.entity.Charger;
import com.example.evsystem.entity.Reservation;
import com.example.evsystem.entity.Vehicle;
import com.example.evsystem.enums.ChargerStatus;
import com.example.evsystem.enums.ChargingSessionStatus;
import com.example.evsystem.enums.ConnectorType;
import com.example.evsystem.enums.PowerOutput;
import com.example.evsystem.enums.ReservationStatus;
import com.example.evsystem.enums.UserRole;
import com.example.evsystem.exception.BusinessException;
import com.example.evsystem.repository.ChargerRepository;
import com.example.evsystem.repository.ChargingSessionRepository;
import com.example.evsystem.repository.ReservationRepository;
import com.example.evsystem.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    private ReservationRepository reservationRepository;
    private VehicleRepository vehicleRepository;
    private ChargerRepository chargerRepository;
    private ChargingSessionRepository chargingSessionRepository;
    private CurrentUserService currentUserService;
    private ReservationService reservationService;

    // --- Helper builder methods ---

    private Vehicle buildVehicle(ConnectorType connectorType) {
        Vehicle v = new Vehicle();
        v.setBrand("Tesla");
        v.setModel("Model 3");
        v.setBatteryCapacity(75.0);
        v.setConnectorType(connectorType);
        v.setPlateNumber("34ABC123");
        return v;
    }

    private Charger buildCharger(ConnectorType connectorType, ChargerStatus status) {
        Charger c = new Charger();
        c.setConnectorType(connectorType);
        c.setStatus(status);
        c.setPricePerKwh(2.5f);
        c.setPowerOutput(PowerOutput.KW_22);
        return c;
    }

    private CreateReservationRequest buildRequest(Long vehicleId, Long chargerId,
                                                  LocalDateTime start, LocalDateTime end) {
        CreateReservationRequest req = new CreateReservationRequest();
        req.setVehicleId(vehicleId);
        req.setChargerId(chargerId);
        req.setStartTime(start);
        req.setEndTime(end);
        return req;
    }

    @BeforeEach
    void setUp() {
        reservationRepository = mock(ReservationRepository.class);
        vehicleRepository = mock(VehicleRepository.class);
        chargerRepository = mock(ChargerRepository.class);
        chargingSessionRepository = mock(ChargingSessionRepository.class);
        currentUserService = mock(CurrentUserService.class);
        when(currentUserService.isCurrentUserAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUsername()).thenReturn(Optional.empty());
        reservationService = new ReservationService(
                reservationRepository, vehicleRepository, chargerRepository, chargingSessionRepository, currentUserService, 24
        );
    }

    // -----------------------------------------------------------------------
    // 1) Can a reservation be created with a compatible vehicle and charger?
    // -----------------------------------------------------------------------
    @Test
    void shouldCreateReservation_whenVehicleAndChargerAreCompatible() {
        Vehicle vehicle = buildVehicle(ConnectorType.TYPE2);
        Charger charger = buildCharger(ConnectorType.TYPE2, ChargerStatus.AVAILABLE);

        LocalDateTime start = LocalDateTime.now().plusMinutes(10);
        LocalDateTime end = start.plusHours(1);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(chargerRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(charger));
        when(reservationRepository.existsByChargerIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                eq(1L), any(), any(), any())).thenReturn(false);
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Reservation result = reservationService.create(buildRequest(1L, 1L, start, end));

        assertNotNull(result);
        assertEquals(ReservationStatus.ACTIVE, result.getStatus());
        verify(reservationRepository).save(any(Reservation.class));
    }

    // -----------------------------------------------------------------------
    // 2) Does the service reject different vehicle and charger connector types?
    // -----------------------------------------------------------------------
    @Test
    void shouldThrowBadRequest_whenConnectorTypesAreIncompatible() {
        Vehicle vehicle = buildVehicle(ConnectorType.TYPE2);
        Charger charger = buildCharger(ConnectorType.CCS, ChargerStatus.AVAILABLE);

        LocalDateTime start = LocalDateTime.now().plusMinutes(10);
        LocalDateTime end = start.plusHours(1);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(chargerRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(charger));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> reservationService.create(buildRequest(1L, 1L, start, end)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(reservationRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // 3) Does the service reject an overlapping reservation for the same charger?
    // -----------------------------------------------------------------------
    @Test
    void shouldThrowConflict_whenChargerAlreadyReservedInSameTimeSlot() {
        Vehicle vehicle = buildVehicle(ConnectorType.TYPE2);
        Charger charger = buildCharger(ConnectorType.TYPE2, ChargerStatus.AVAILABLE);

        LocalDateTime start = LocalDateTime.now().plusMinutes(10);
        LocalDateTime end = start.plusHours(1);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(chargerRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(charger));
        when(reservationRepository.existsByChargerIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                eq(1L), any(), any(), any())).thenReturn(true); // overlap exists

        BusinessException ex = assertThrows(BusinessException.class,
                () -> reservationService.create(buildRequest(1L, 1L, start, end)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(reservationRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // 4) Does the service reject reservations longer than two hours?
    // -----------------------------------------------------------------------
    @Test
    void shouldThrowBadRequest_whenReservationExceedsTwoHours() {
        Vehicle vehicle = buildVehicle(ConnectorType.TYPE2);
        Charger charger = buildCharger(ConnectorType.TYPE2, ChargerStatus.AVAILABLE);

        LocalDateTime start = LocalDateTime.now().plusMinutes(10);
        LocalDateTime end = start.plusHours(3); // 3 hours is invalid

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(chargerRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(charger));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> reservationService.create(buildRequest(1L, 1L, start, end)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(reservationRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // 5) Does the service reject reservations that start in the past?
    // -----------------------------------------------------------------------
    @Test
    void shouldThrowBadRequest_whenStartTimeIsInThePast() {
        Vehicle vehicle = buildVehicle(ConnectorType.TYPE2);
        Charger charger = buildCharger(ConnectorType.TYPE2, ChargerStatus.AVAILABLE);

        LocalDateTime start = LocalDateTime.now().minusHours(1); // past
        LocalDateTime end = LocalDateTime.now().plusMinutes(30);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(chargerRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(charger));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> reservationService.create(buildRequest(1L, 1L, start, end)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(reservationRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // 6) Does the service reject reservations when the charger is not available?
    // -----------------------------------------------------------------------
    @Test
    void shouldThrowConflict_whenChargerIsNotAvailable() {
        Vehicle vehicle = buildVehicle(ConnectorType.TYPE2);
        Charger charger = buildCharger(ConnectorType.TYPE2, ChargerStatus.OCCUPIED); // not available

        LocalDateTime start = LocalDateTime.now().plusMinutes(10);
        LocalDateTime end = start.plusHours(1);
        Reservation activeReservation = new Reservation();
        activeReservation.setEndTime(LocalDateTime.now().plusMinutes(30));

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(chargerRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(charger));
        when(reservationRepository.findFirstByChargerIdAndStatusInAndEndTimeAfterOrderByEndTimeAsc(
                eq(charger.getId()), anyList(), any(LocalDateTime.class)))
                .thenReturn(Optional.of(activeReservation));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> reservationService.create(buildRequest(1L, 1L, start, end)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void shouldHideVehicle_whenReservationVehicleBelongsToAnotherUser() {
        AppUser owner = new AppUser();
        owner.setUsername("bob");
        owner.setPasswordHash("hash");
        owner.setRole(UserRole.ROLE_USER);

        Vehicle vehicle = buildVehicle(ConnectorType.TYPE2);
        vehicle.setOwner(owner);
        Charger charger = buildCharger(ConnectorType.TYPE2, ChargerStatus.AVAILABLE);

        LocalDateTime start = LocalDateTime.now().plusMinutes(10);
        LocalDateTime end = start.plusHours(1);

        when(currentUserService.getCurrentUsername()).thenReturn(Optional.of("alice"));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(chargerRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(charger));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> reservationService.create(buildRequest(1L, 1L, start, end)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        verify(reservationRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // Existing tests kept as-is
    // -----------------------------------------------------------------------
    @Test
    void cancelRejectsInProgressReservations() {
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.IN_PROGRESS);

        when(reservationRepository.findById(3L)).thenReturn(Optional.of(reservation));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> reservationService.cancel(3L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void deleteRejectsReservationsWithSessionHistory() {
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.EXPIRED);

        when(reservationRepository.findById(4L)).thenReturn(Optional.of(reservation));
        when(chargingSessionRepository.existsByReservationIdAndStatus(4L, ChargingSessionStatus.ACTIVE)).thenReturn(false);
        when(chargingSessionRepository.existsByReservationId(4L)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> reservationService.delete(4L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(reservationRepository, never()).delete(any(Reservation.class));
    }

    @Test
    void autoExpireMarksElapsedActiveReservations() {
        Reservation reservation = new Reservation();
        reservation.setVehicle(new Vehicle());
        reservation.setCharger(new Charger());
        reservation.setStatus(ReservationStatus.ACTIVE);
        reservation.setEndTime(LocalDateTime.now().minusMinutes(1));
        try {
            java.lang.reflect.Field idField = Reservation.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(reservation, 5L);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }

        when(reservationRepository.findByStatusInAndEndTimeLessThanEqual(
                eq(List.of(ReservationStatus.ACTIVE)), any(LocalDateTime.class)))
                .thenReturn(List.of(reservation));
        when(chargingSessionRepository.existsByReservationIdAndStatus(any(), any()))
                .thenReturn(false);
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        reservationService.autoExpireReservationsWithoutSession();

        assertEquals(ReservationStatus.EXPIRED, reservation.getStatus());
        verify(reservationRepository).save(reservation);
    }
}
