package com.example.evsystem.service;

import com.example.evsystem.entity.Charger;
import com.example.evsystem.repository.ChargerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChargerService {

    private final ChargerRepository chargerRepository;

    public ChargerService(ChargerRepository chargerRepository) {
        this.chargerRepository = chargerRepository;
    }

    public Charger save(Charger charger) {
        return chargerRepository.save(charger);
    }

    public List<Charger> getAll() {
        return chargerRepository.findAll();
    }

    public Charger getById(Long id) {
        return chargerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charger not found"));
    }

    public List<Charger> getByStationId(Long stationId) {
        return chargerRepository.findAll().stream()
                .filter(c -> c.getStation() != null && c.getStation().getId().equals(stationId))
                .toList();
    }
}