package com.example.evsystem.controller;

import com.example.evsystem.dto.ChargingSessionResponse;
import com.example.evsystem.service.ChargingSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sessions")
@Validated
@Tag(name = "Charging Sessions", description = "Charging session lifecycle endpoints")
public class ChargingSessionController {

    private final ChargingSessionService chargingSessionService;

    public ChargingSessionController(ChargingSessionService chargingSessionService) {
        this.chargingSessionService = chargingSessionService;
    }

    @Operation(summary = "Start charging session", description = "Starts a charging session for a reservation.")
    @PostMapping("/start")
    public ChargingSessionResponse startSession(@RequestParam @Positive(message = "Reservation id must be greater than zero.") Long reservationId,
                                                @RequestParam(required = false) @DecimalMin(value = "0.0", message = "Start battery level must be between 0 and 100.") @DecimalMax(value = "100.0", message = "Start battery level must be between 0 and 100.") Float startBatteryLevel) {
        return ChargingSessionResponse.from(chargingSessionService.startSession(reservationId, startBatteryLevel));
    }

    @Operation(summary = "End charging session", description = "Ends an active charging session.")
    @PostMapping("/{id}/end")
    public ChargingSessionResponse endSession(@PathVariable @Positive(message = "Session id must be greater than zero.") Long id,
                                              @RequestParam @DecimalMin(value = "0.0", message = "End battery level must be between 0 and 100.") @DecimalMax(value = "100.0", message = "End battery level must be between 0 and 100.") Float endBatteryLevel) {
        return ChargingSessionResponse.from(chargingSessionService.endSession(id, endBatteryLevel));
    }

    @Operation(summary = "List sessions", description = "Returns all charging sessions.")
    @GetMapping
    public List<ChargingSessionResponse> getAllSessions() {
        return chargingSessionService.getAllSessions().stream()
                .map(ChargingSessionResponse::from)
                .toList();
    }

    @Operation(summary = "Get session by id", description = "Returns one charging session by its id.")
    @GetMapping("/{id}")
    public ChargingSessionResponse getSession(@PathVariable @Positive(message = "Session id must be greater than zero.") Long id) {
        return ChargingSessionResponse.from(chargingSessionService.getSessionById(id));
    }
}
