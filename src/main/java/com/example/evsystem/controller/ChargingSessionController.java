package com.example.evsystem.controller;

import com.example.evsystem.dto.ChargingSessionResponse;
import com.example.evsystem.service.ChargingSessionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sessions")
public class ChargingSessionController {

    private final ChargingSessionService chargingSessionService;

    public ChargingSessionController(ChargingSessionService chargingSessionService) {
        this.chargingSessionService = chargingSessionService;
    }

    @PostMapping("/start")
    public ChargingSessionResponse startSession(@RequestParam Long reservationId,
                                                @RequestParam(required = false) Float startBatteryLevel) {
        return ChargingSessionResponse.from(chargingSessionService.startSession(reservationId, startBatteryLevel));
    }

    @PostMapping("/{id}/end")
    public ChargingSessionResponse endSession(@PathVariable Long id,
                                              @RequestParam Float endBatteryLevel) {
        return ChargingSessionResponse.from(chargingSessionService.endSession(id, endBatteryLevel));
    }

    @GetMapping
    public List<ChargingSessionResponse> getAllSessions() {
        return chargingSessionService.getAllSessions().stream()
                .map(ChargingSessionResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ChargingSessionResponse getSession(@PathVariable Long id) {
        return ChargingSessionResponse.from(chargingSessionService.getSessionById(id));
    }
}
