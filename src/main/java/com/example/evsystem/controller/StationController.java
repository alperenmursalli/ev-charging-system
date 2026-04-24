package com.example.evsystem.controller;

import com.example.evsystem.entity.Charger;
import com.example.evsystem.entity.Station;
import com.example.evsystem.service.ChargerService;
import com.example.evsystem.service.StationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stations")
public class StationController {

    private final StationService stationService;
    private final ChargerService chargerService;

    public StationController(StationService stationService, ChargerService chargerService) {
        this.stationService = stationService;
        this.chargerService = chargerService;
    }

    @PostMapping
    public Station createStation(@RequestBody Station station) {
        return stationService.save(station);
    }

    @GetMapping
    public List<Station> getAllStations() {
        return stationService.getAll();
    }

    @GetMapping("/{id}")
    public Station getStationById(@PathVariable Long id) {
        return stationService.getById(id);
    }

    @GetMapping("/{id}/chargers")
    public List<Charger> getChargersByStation(@PathVariable Long id) {
        return chargerService.getByStationId(id);
    }
}