package com.example.evsystem.service;

import com.example.evsystem.entity.Charger;
import com.example.evsystem.entity.ChargingSession;
import com.example.evsystem.entity.Reservation;
import com.example.evsystem.enums.ChargingSessionStatus;
import com.example.evsystem.enums.ChargerStatus;
import com.example.evsystem.enums.PowerOutput;
import com.example.evsystem.enums.ReservationStatus;
import com.example.evsystem.exception.BusinessException;
import com.example.evsystem.repository.ChargerRepository;
import com.example.evsystem.repository.ChargingSessionRepository;
import com.example.evsystem.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChargingSessionServiceTest {

    private final ChargingSessionRepository chargingSessionRepository = mock(ChargingSessionRepository.class);
    private final ReservationRepository reservationRepository = mock(ReservationRepository.class);
    private final ChargerRepository chargerRepository = mock(ChargerRepository.class);
    private final ReservationService reservationService = mock(ReservationService.class);
    private final ChargingSessionService chargingSessionService =
            new ChargingSessionService(chargingSessionRepository, reservationRepository, chargerRepository, reservationService, 20f);

    @Test
    void startSessionRejectsOccupiedCharger() {
        Charger charger = mock(Charger.class);
        when(charger.getId()).thenReturn(20L);
        when(charger.getStatus()).thenReturn(ChargerStatus.OCCUPIED);

        Reservation reservation = mock(Reservation.class);
        when(reservation.getId()).thenReturn(10L);
        when(reservation.getStatus()).thenReturn(ReservationStatus.ACTIVE);
        when(reservation.getCharger()).thenReturn(charger);

        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
        when(chargerRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(charger));
        when(chargingSessionRepository.findByReservationIdAndStatus(10L, ChargingSessionStatus.ACTIVE)).thenReturn(Optional.empty());
        when(chargingSessionRepository.existsByReservation_Charger_IdAndStatus(20L, ChargingSessionStatus.ACTIVE)).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> chargingSessionService.startSession(10L, 45f)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(chargingSessionRepository, never()).save(any(ChargingSession.class));
    }

    @Test
    void endSessionCompletesSessionAndReleasesCharger() {
        Charger charger = mock(Charger.class);
        when(charger.getId()).thenReturn(30L);
        when(charger.getPricePerKwh()).thenReturn(8f);

        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.ACTIVE);
        reservation.setCharger(charger);
        reservation.setVehicle(vehicleWithBatteryCapacity(80d));

        ChargingSession session = new ChargingSession();
        session.setReservation(reservation);
        session.setStatus(ChargingSessionStatus.ACTIVE);
        session.setStartBatteryLevel(20f);

        when(chargingSessionRepository.findById(5L)).thenReturn(Optional.of(session));
        when(chargerRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(charger));
        when(chargingSessionRepository.save(any(ChargingSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChargingSession result = chargingSessionService.endSession(5L, 50f);

        assertEquals(24f, result.getConsumedKwh());
        assertEquals(192f, result.getTotalCost());
        verify(charger).setStatus(ChargerStatus.AVAILABLE);
        verify(reservationService).markCompleted(any());
        assertEquals(ChargingSessionStatus.COMPLETED, result.getStatus());
    }

    @Test
    void autoCompleteExpiredSessionsCalculatesBatteryAndCostFromPowerOutput() {
        Charger charger = mock(Charger.class);
        when(charger.getId()).thenReturn(40L);
        when(charger.getPricePerKwh()).thenReturn(10f);
        when(charger.getPowerOutput()).thenReturn(PowerOutput.KW_22);

        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.ACTIVE);
        reservation.setEndTime(LocalDateTime.now().minusMinutes(1));
        reservation.setVehicle(vehicleWithBatteryCapacity(80d));
        reservation.setCharger(charger);

        ChargingSession session = new ChargingSession();
        session.setReservation(reservation);
        session.setStatus(ChargingSessionStatus.ACTIVE);
        session.setStartBatteryLevel(20f);
        session.setStartedAt(LocalDateTime.now().minusHours(2));

        when(chargingSessionRepository.findByStatusAndReservation_EndTimeLessThanEqual(any(ChargingSessionStatus.class), any(LocalDateTime.class)))
                .thenReturn(java.util.List.of(session));
        when(chargerRepository.findByIdForUpdate(40L)).thenReturn(Optional.of(charger));
        when(chargingSessionRepository.save(any(ChargingSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        chargingSessionService.autoCompleteExpiredSessions();

        assertEquals(64f, session.getConsumedKwh());
        assertEquals(100f, session.getEndBatteryLevel());
        assertEquals(640f, session.getTotalCost());
        assertEquals(ChargingSessionStatus.COMPLETED, session.getStatus());
        verify(reservationService).markCompleted(any());
    }

    @Test
    void autoStartDueReservationsCreatesSessionWithConfiguredBatteryLevel() {
        Charger charger = mock(Charger.class);
        when(charger.getId()).thenReturn(50L);
        when(charger.getStatus()).thenReturn(ChargerStatus.AVAILABLE);

        Reservation reservation = mock(Reservation.class);
        when(reservation.getId()).thenReturn(15L);
        when(reservation.getStatus()).thenReturn(ReservationStatus.ACTIVE);
        when(reservation.getCharger()).thenReturn(charger);
        when(reservation.getStartTime()).thenReturn(LocalDateTime.now().minusMinutes(1));
        when(reservation.getEndTime()).thenReturn(LocalDateTime.now().plusMinutes(5));

        when(reservationRepository.findByStatusAndStartTimeLessThanEqualAndEndTimeGreaterThan(
                any(ReservationStatus.class), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(java.util.List.of(reservation));
        when(chargingSessionRepository.findByReservationIdAndStatus(15L, ChargingSessionStatus.ACTIVE)).thenReturn(Optional.empty());
        when(reservationRepository.findById(15L)).thenReturn(Optional.of(reservation));
        when(chargerRepository.findByIdForUpdate(50L)).thenReturn(Optional.of(charger));
        when(chargingSessionRepository.existsByReservation_Charger_IdAndStatus(50L, ChargingSessionStatus.ACTIVE)).thenReturn(false);
        when(chargingSessionRepository.save(any(ChargingSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        chargingSessionService.autoStartDueReservations();

        verify(chargingSessionRepository).save(any(ChargingSession.class));
        verify(charger).setStatus(ChargerStatus.OCCUPIED);
        verify(reservationService).markInProgress(15L);
    }

    private com.example.evsystem.entity.Vehicle vehicleWithBatteryCapacity(Double batteryCapacity) {
        com.example.evsystem.entity.Vehicle vehicle = new com.example.evsystem.entity.Vehicle();
        vehicle.setBatteryCapacity(batteryCapacity);
        return vehicle;
    }
}
