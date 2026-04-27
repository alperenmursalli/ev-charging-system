package com.example.evsystem.service;

import com.example.evsystem.dto.ChargerRequest;
import com.example.evsystem.dto.ChargerUpdateRequest;
import com.example.evsystem.entity.Charger;
import com.example.evsystem.entity.Station;
import com.example.evsystem.exception.BusinessException;
import com.example.evsystem.repository.ChargerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ChargerService {

    private final ChargerRepository chargerRepository;
    private final StationService stationService;

    public ChargerService(ChargerRepository chargerRepository, StationService stationService) {
        this.chargerRepository = chargerRepository;
        this.stationService = stationService;
    }

    public Charger save(ChargerRequest request) {
        if (request.getPricePerKwh() != null && request.getPricePerKwh() < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Price per kWh cannot be negative");
        }
        if (request.getChargerCode() != null && chargerRepository.existsByChargerCodeIgnoreCase(request.getChargerCode().trim())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Charger with this code already exists");
        }

        Station station = stationService.getById(request.getStationId());
        Charger charger = new Charger();
        charger.setChargerCode(request.getChargerCode().trim());
        charger.setChargerType(request.getChargerType());
        charger.setPowerOutput(request.getPowerOutput());
        charger.setConnectorType(request.getConnectorType());
        charger.setPricePerKwh(request.getPricePerKwh());
        charger.setStatus(request.getStatus());
        charger.setStation(station);
        return chargerRepository.save(charger);
    }

    public List<Charger> getAll() {
        return chargerRepository.findAll();
    }

    public Charger getById(Long id) {
        return chargerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Charger not found with id: " + id));
    }

    public List<Charger> getByStationId(Long stationId) {
        return chargerRepository.findByStationId(stationId);
    }

    public Charger update(Long id, ChargerUpdateRequest updated) {
        Charger charger = getById(id);
        if (updated.getPricePerKwh() != null && updated.getPricePerKwh() < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Price per kWh cannot be negative");
        }
        if (updated.getChargerCode() != null) {
            if (updated.getChargerCode().isBlank()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Charger code cannot be blank");
            }
            String chargerCode = updated.getChargerCode().trim();
            if (chargerRepository.existsByChargerCodeIgnoreCaseAndIdNot(chargerCode, id)) {
                throw new BusinessException(HttpStatus.CONFLICT, "Charger with this code already exists");
            }
            charger.setChargerCode(chargerCode);
        }
        if (updated.getChargerType() != null) charger.setChargerType(updated.getChargerType());
        if (updated.getPowerOutput() != null) charger.setPowerOutput(updated.getPowerOutput());
        if (updated.getConnectorType() != null) charger.setConnectorType(updated.getConnectorType());
        if (updated.getPricePerKwh() != null) charger.setPricePerKwh(updated.getPricePerKwh());
        if (updated.getStatus() != null) charger.setStatus(updated.getStatus());
        if (updated.getStationId() != null) {
            charger.setStation(stationService.getById(updated.getStationId()));
        }
        return chargerRepository.save(charger);
    }

    public void delete(Long id) {
        getById(id);
        chargerRepository.deleteById(id);
    }
}
