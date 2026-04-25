package com.example.evsystem.service;

import com.example.evsystem.entity.Charger;
import com.example.evsystem.entity.ChargingSession;
import com.example.evsystem.entity.Reservation;
import com.example.evsystem.enums.ChargingSessionStatus;
import com.example.evsystem.enums.ChargerStatus;
import com.example.evsystem.enums.ReservationStatus;
import com.example.evsystem.exception.BusinessException;
import com.example.evsystem.repository.ChargerRepository;
import com.example.evsystem.repository.ChargingSessionRepository;
import com.example.evsystem.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

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
    private final ChargingSessionService chargingSessionService =
            new ChargingSessionService(chargingSessionRepository, reservationRepository, chargerRepository);

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

        ChargingSession session = new ChargingSession();
        session.setReservation(reservation);
        session.setStatus(ChargingSessionStatus.ACTIVE);

        when(chargingSessionRepository.findById(5L)).thenReturn(Optional.of(session));
        when(chargerRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(charger));
        when(chargingSessionRepository.save(any(ChargingSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChargingSession result = chargingSessionService.endSession(5L, 90f, 12f);

        assertEquals(96f, result.getTotalCost());
        verify(charger).setStatus(ChargerStatus.AVAILABLE);
        assertEquals(ReservationStatus.COMPLETED, reservation.getStatus());
        assertEquals(ChargingSessionStatus.COMPLETED, result.getStatus());
    }
}
