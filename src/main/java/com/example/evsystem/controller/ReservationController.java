package com.example.evsystem.controller;


import com.example.evsystem.dto.CreateReservationRequest;
import com.example.evsystem.dto.ReservationResponse;
import com.example.evsystem.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PatchMapping("/{id}/cancel")
    public void cancelReservation(@PathVariable Long id) {
        reservationService.cancel(id);
    }

    @PostMapping
    public ReservationResponse createReservation(@Valid @RequestBody CreateReservationRequest request) {
        return ReservationResponse.from(reservationService.create(request));
    }
}
