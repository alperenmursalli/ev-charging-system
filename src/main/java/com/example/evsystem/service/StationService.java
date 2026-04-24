package com.example.evsystem.service;

import com.example.evsystem.entity.Station;
import com.example.evsystem.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StationService {

    private final StationRepository stationRepository;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public Station save(Station station) {
        return stationRepository.save(station);
    }

    public List<Station> getAll() {
        return stationRepository.findAll();
    }

    public Station getById(Long id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Station not found"));
    }
}