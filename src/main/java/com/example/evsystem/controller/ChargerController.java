package com.example.evsystem.controller;

import com.example.evsystem.entity.Charger;
import com.example.evsystem.service.ChargerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chargers")
public class ChargerController {

    private final ChargerService chargerService;

    public ChargerController(ChargerService chargerService) {
        this.chargerService = chargerService;
    }

    @PostMapping
    public Charger createCharger(@RequestBody Charger charger) {
        return chargerService.save(charger);
    }

    @GetMapping
    public List<Charger> getAllChargers() {
        return chargerService.getAll();
    }

    @GetMapping("/{id}")
    public Charger getChargerById(@PathVariable Long id) {
        return chargerService.getById(id);
    }
}