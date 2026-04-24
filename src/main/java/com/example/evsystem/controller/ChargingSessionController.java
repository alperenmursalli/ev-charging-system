package com.example.evsystem.controller;

import com.example.evsystem.entity.ChargingSession;
import com.example.evsystem.service.ChargingSessionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sessions")
public class ChargingSessionController {

    private final ChargingSessionService chargingSessionService;

    public ChargingSessionController(ChargingSessionService chargingSessionService) {
        this.chargingSessionService = chargingSessionService;
    }

    @PostMapping("/start")
    public ChargingSession startSession(@RequestParam Long reservationId,
                                        @RequestParam Float startBatteryLevel) {
        return chargingSessionService.startSession(reservationId, startBatteryLevel);
    }

    @PostMapping("/{id}/end")
    public ChargingSession endSession(@PathVariable Long id,
                                      @RequestParam Float endBatteryLevel,
                                      @RequestParam Float consumedKwh) {
        return chargingSessionService.endSession(id, endBatteryLevel, consumedKwh);
    }

    @GetMapping
    public List<ChargingSession> getAllSessions() {
        return chargingSessionService.getAllSessions();
    }

    @GetMapping("/{id}")
    public ChargingSession getSession(@PathVariable Long id) {
        return chargingSessionService.getSessionById(id);
    }
}