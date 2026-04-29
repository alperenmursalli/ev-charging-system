package com.example.evsystem.service;

import com.example.evsystem.entity.Charger;
import com.example.evsystem.entity.ChargingSession;
import com.example.evsystem.entity.Reservation;
import com.example.evsystem.entity.Vehicle;
import com.example.evsystem.enums.ChargerStatus;
import com.example.evsystem.enums.ChargingSessionStatus;
import com.example.evsystem.enums.ConnectorType;
import com.example.evsystem.enums.PowerOutput;
import com.example.evsystem.enums.ReservationStatus;
import com.example.evsystem.exception.BusinessException;
import com.example.evsystem.repository.ChargerRepository;
import com.example.evsystem.repository.ChargingSessionRepository;
import com.example.evsystem.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChargingSessionServiceTest {

    private ChargingSessionRepository chargingSessionRepository;
    private ReservationRepository reservationRepository;
    private ChargerRepository chargerRepository;
    private ReservationService reservationService;
    private ChargingSessionService chargingSessionService;

    // --- Helper builder methods ---

    private Vehicle buildVehicle(double batteryCapacity) {
        Vehicle v = new Vehicle();
        v.setBrand("Tesla");
        v.setModel("Model 3");
        v.setBatteryCapacity(batteryCapacity);
        v.setConnectorType(ConnectorType.TYPE2);
        v.setPlateNumber("34XYZ999");
        return v;
    }

    private Charger buildCharger(float pricePerKwh) {
        Charger c = new Charger();
        c.setConnectorType(ConnectorType.TYPE2);
        c.setStatus(ChargerStatus.OCCUPIED);
        c.setPricePerKwh(pricePerKwh);
        c.setPowerOutput(PowerOutput.KW_22);
        return c;
    }

    private ChargingSession buildActiveSession(Vehicle vehicle, Charger charger,
                                               float startBatteryLevel,
                                               LocalDateTime startedAt,
                                               LocalDateTime reservationEnd) {
        Reservation reservation = new Reservation();
        reservation.setVehicle(vehicle);
        reservation.setCharger(charger);
        reservation.setStatus(ReservationStatus.IN_PROGRESS);
        reservation.setStartTime(startedAt.minusMinutes(5));
        reservation.setEndTime(reservationEnd);

        // Set reservation id through reflection.
        try {
            java.lang.reflect.Field idField = Reservation.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(reservation, 10L);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }

        ChargingSession session = new ChargingSession();
        session.setReservation(reservation);
        session.setStartBatteryLevel(startBatteryLevel);
        session.setStartedAt(startedAt);
        session.setStatus(ChargingSessionStatus.ACTIVE);
        return session;
    }

    @BeforeEach
    void setUp() {
        chargingSessionRepository = mock(ChargingSessionRepository.class);
        reservationRepository = mock(ReservationRepository.class);
        chargerRepository = mock(ChargerRepository.class);
        reservationService = mock(ReservationService.class);
        chargingSessionService = new ChargingSessionService(
                chargingSessionRepository, reservationRepository, chargerRepository, reservationService, 20f
        );
    }

    // -----------------------------------------------------------------------
    // 1) Is consumed kWh calculated correctly?
    //    Formula: batteryCapacity * (endBattery - startBattery) / 100
    //    75 kWh * (80 - 20) / 100 = 45.00 kWh
    // -----------------------------------------------------------------------
    @Test
    void shouldCalculateConsumedKwh_correctly() {
        Vehicle vehicle = buildVehicle(75.0);
        Charger charger = buildCharger(2.0f);

        ChargingSession session = buildActiveSession(vehicle, charger, 20f,
                LocalDateTime.now().minusMinutes(30),
                LocalDateTime.now().plusMinutes(30));

        // Set session id through reflection.
        try {
            java.lang.reflect.Field idField = ChargingSession.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(session, 1L);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }

        when(chargingSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(chargerRepository.findByIdForUpdate(any())).thenReturn(Optional.of(charger));
        when(chargingSessionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ChargingSession result = chargingSessionService.endSession(1L, 80f);

        // 75 * (80-20) / 100 = 45.00
        assertEquals(45.00f, result.getConsumedKwh(), 0.01f);
    }

    // -----------------------------------------------------------------------
    // 2) Is total cost calculated correctly?
    //    consumedKwh * pricePerKwh = 45.00 * 2.0 = 90.00
    // -----------------------------------------------------------------------
    @Test
    void shouldCalculateTotalCost_correctly() {
        Vehicle vehicle = buildVehicle(75.0);
        Charger charger = buildCharger(2.0f); // 2.0 TL/kWh

        ChargingSession session = buildActiveSession(vehicle, charger, 20f,
                LocalDateTime.now().minusMinutes(30),
                LocalDateTime.now().plusMinutes(30));

        try {
            java.lang.reflect.Field idField = ChargingSession.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(session, 2L);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }

        when(chargingSessionRepository.findById(2L)).thenReturn(Optional.of(session));
        when(chargerRepository.findByIdForUpdate(any())).thenReturn(Optional.of(charger));
        when(chargingSessionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ChargingSession result = chargingSessionService.endSession(2L, 80f);

        // 45.00 kWh * 2.0 TL = 90.00 TL
        assertEquals(90.00f, result.getTotalCost(), 0.01f);
    }

    // -----------------------------------------------------------------------
    // 3) Is the status set to COMPLETED when the session ends?
    // -----------------------------------------------------------------------
    @Test
    void shouldSetStatusCompleted_whenSessionEnds() {
        Vehicle vehicle = buildVehicle(60.0);
        Charger charger = buildCharger(3.0f);

        ChargingSession session = buildActiveSession(vehicle, charger, 30f,
                LocalDateTime.now().minusMinutes(20),
                LocalDateTime.now().plusMinutes(40));

        try {
            java.lang.reflect.Field idField = ChargingSession.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(session, 3L);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }

        when(chargingSessionRepository.findById(3L)).thenReturn(Optional.of(session));
        when(chargerRepository.findByIdForUpdate(any())).thenReturn(Optional.of(charger));
        when(chargingSessionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ChargingSession result = chargingSessionService.endSession(3L, 70f);

        assertEquals(ChargingSessionStatus.COMPLETED, result.getStatus());
    }

    // -----------------------------------------------------------------------
    // 4) Does the service reject an end battery level below the start battery level?
    // -----------------------------------------------------------------------
    @Test
    void shouldThrowBadRequest_whenEndBatteryLowerThanStartBattery() {
        Vehicle vehicle = buildVehicle(75.0);
        Charger charger = buildCharger(2.0f);

        ChargingSession session = buildActiveSession(vehicle, charger, 60f,
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().plusMinutes(50));

        try {
            java.lang.reflect.Field idField = ChargingSession.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(session, 4L);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }

        when(chargingSessionRepository.findById(4L)).thenReturn(Optional.of(session));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> chargingSessionService.endSession(4L, 40f)); // 40 < 60 is invalid

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(chargingSessionRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // 5) Are expired active sessions completed by the scheduler?
    // -----------------------------------------------------------------------
    @Test
    void shouldAutoComplete_expiredActiveSessions() {
        Vehicle vehicle = buildVehicle(100.0);
        Charger charger = buildCharger(1.5f);

        // Reservation window has already ended.
        ChargingSession session = buildActiveSession(vehicle, charger, 10f,
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusMinutes(1)); // endTime is in the past

        when(chargingSessionRepository.findByStatusAndReservation_EndTimeLessThanEqual(
                eq(ChargingSessionStatus.ACTIVE), any())).thenReturn(java.util.List.of(session));
        when(chargerRepository.findByIdForUpdate(any())).thenReturn(Optional.of(charger));
        when(chargingSessionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        chargingSessionService.autoCompleteExpiredSessions();

        assertEquals(ChargingSessionStatus.COMPLETED, session.getStatus());
        verify(chargingSessionRepository).save(session);
    }
}
