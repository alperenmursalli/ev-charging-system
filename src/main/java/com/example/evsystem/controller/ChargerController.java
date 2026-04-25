package com.example.evsystem.controller;

import com.example.evsystem.entity.Charger;
import com.example.evsystem.service.ChargerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Charger> createCharger(@Valid @RequestBody Charger charger) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chargerService.save(charger));
    }

    @GetMapping
    public ResponseEntity<List<Charger>> getAllChargers() {
        return ResponseEntity.ok(chargerService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Charger> getChargerById(@PathVariable Long id) {
        return ResponseEntity.ok(chargerService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Charger> updateCharger(@PathVariable Long id, @Valid @RequestBody Charger charger) {
        return ResponseEntity.ok(chargerService.update(id, charger));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCharger(@PathVariable Long id) {
        chargerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}