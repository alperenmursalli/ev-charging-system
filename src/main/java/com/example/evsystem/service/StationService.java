package com.example.evsystem.service;

import com.example.evsystem.entity.Station;
import com.example.evsystem.exception.BusinessException;
import com.example.evsystem.repository.StationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StationService {

    private final StationRepository stationRepository;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public Station save(Station station) {
        if (station.getName() == null || station.getName().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Station name cannot be empty");
        }
        if (stationRepository.existsByNameIgnoreCase(station.getName())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Station with this name already exists");
        }
        return stationRepository.save(station);
    }

    public List<Station> getAll() {
        return stationRepository.findAll();
    }

    public Station getById(Long id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Station not found with id: " + id));
    }

    public Station update(Long id, Station updated) {
        Station station = getById(id);
        if (updated.getName() != null) station.setName(updated.getName());
        if (updated.getAddress() != null) station.setAddress(updated.getAddress());
        if (updated.getLatitude() != null) station.setLatitude(updated.getLatitude());
        if (updated.getLongitude() != null) station.setLongitude(updated.getLongitude());
        if (updated.getStatus() != null) station.setStatus(updated.getStatus());
        return stationRepository.save(station);
    }

    public void delete(Long id) {
        getById(id);
        stationRepository.deleteById(id);
    }
}