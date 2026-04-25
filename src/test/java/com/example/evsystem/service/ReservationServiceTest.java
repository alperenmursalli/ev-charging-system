package com.example.evsystem.service;

import com.example.evsystem.entity.Reservation;
import com.example.evsystem.enums.ChargingSessionStatus;
import com.example.evsystem.enums.ReservationStatus;
import com.example.evsystem.exception.BusinessException;
import com.example.evsystem.repository.ChargerRepository;
import com.example.evsystem.repository.ChargingSessionRepository;
import com.example.evsystem.repository.ReservationRepository;
import com.example.evsystem.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservationServiceTest {

    private final ReservationRepository reservationRepository = mock(ReservationRepository.class);
    private final VehicleRepository vehicleRepository = mock(VehicleRepository.class);
    private final ChargerRepository chargerRepository = mock(ChargerRepository.class);
    private final ChargingSessionRepository chargingSessionRepository = mock(ChargingSessionRepository.class);
    private final ReservationService reservationService =
            new ReservationService(reservationRepository, vehicleRepository, chargerRepository, chargingSessionRepository, 24);

    @Test
    void cancelRejectsInProgressReservations() {
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.IN_PROGRESS);

        when(reservationRepository.findById(3L)).thenReturn(Optional.of(reservation));

        BusinessException exception = assertThrows(BusinessException.class, () -> reservationService.cancel(3L));

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

        BusinessException exception = assertThrows(BusinessException.class, () -> reservationService.delete(4L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(reservationRepository, never()).delete(any(Reservation.class));
    }

    @Test
    void autoExpireMarksElapsedActiveReservations() {
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.ACTIVE);
        reservation.setEndTime(LocalDateTime.now().minusMinutes(1));

        when(reservationRepository.findByStatusInAndEndTimeLessThanEqual(List.of(ReservationStatus.ACTIVE), any(LocalDateTime.class)))
                .thenReturn(List.of(reservation));
        when(chargingSessionRepository.existsByReservationIdAndStatus(any(Long.class), any(ChargingSessionStatus.class)))
                .thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        reservationService.autoExpireReservationsWithoutSession();

        assertEquals(ReservationStatus.EXPIRED, reservation.getStatus());
        verify(reservationRepository).save(reservation);
    }
}
