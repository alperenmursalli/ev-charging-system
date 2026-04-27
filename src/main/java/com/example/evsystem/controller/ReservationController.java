package com.example.evsystem.controller;


import com.example.evsystem.dto.CreateReservationRequest;
import com.example.evsystem.dto.ReservationResponse;
import com.example.evsystem.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservations")
@Validated
@Tag(name = "Reservations", description = "Reservation management endpoints")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Operation(summary = "Cancel reservation", description = "Cancels a reservation if it has not started charging.")
    @PatchMapping("/{id}/cancel")
    public void cancelReservation(@PathVariable @Positive(message = "Reservation id must be greater than zero.") Long id) {
        reservationService.cancel(id);
    }

    @Operation(summary = "Delete reservation", description = "Deletes a reservation if no charging session history exists.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable @Positive(message = "Reservation id must be greater than zero.") Long id) {
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List reservations", description = "Returns all reservations.")
    @GetMapping
    public List<ReservationResponse> getAllReservations() {
        return reservationService.getAllResponses();
    }

    @Operation(summary = "Get reservation by id", description = "Returns one reservation by its id.")
    @GetMapping("/{id}")
    public ReservationResponse getReservationById(@PathVariable @Positive(message = "Reservation id must be greater than zero.") Long id) {
        return reservationService.getResponseById(id);
    }

    @Operation(summary = "Create reservation", description = "Creates a reservation after validating time window, overlap, charger status, and connector compatibility.")
    @PostMapping
    public ReservationResponse createReservation(@Valid @RequestBody CreateReservationRequest request) {
        return reservationService.createResponse(request);
    }
}
