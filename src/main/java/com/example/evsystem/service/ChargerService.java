package com.example.evsystem.service;

import com.example.evsystem.entity.Charger;
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

    public ChargerService(ChargerRepository chargerRepository) {
        this.chargerRepository = chargerRepository;
    }

    public Charger save(Charger charger) {
        if (charger.getPricePerKwh() != null && charger.getPricePerKwh() < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Price per kWh cannot be negative");
        }
        if (charger.getChargerCode() != null && chargerRepository.existsByChargerCodeIgnoreCase(charger.getChargerCode())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Charger with this code already exists");
        }
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

    public Charger update(Long id, Charger updated) {
        Charger charger = getById(id);
        if (updated.getPricePerKwh() != null && updated.getPricePerKwh() < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Price per kWh cannot be negative");
        }
        if (updated.getChargerCode() != null) {
            if (chargerRepository.existsByChargerCodeIgnoreCaseAndIdNot(updated.getChargerCode(), id)) {
                throw new BusinessException(HttpStatus.CONFLICT, "Charger with this code already exists");
            }
            charger.setChargerCode(updated.getChargerCode());
        }
        if (updated.getChargerType() != null) charger.setChargerType(updated.getChargerType());
        if (updated.getPowerOutput() != null) charger.setPowerOutput(updated.getPowerOutput());
        if (updated.getConnectorType() != null) charger.setConnectorType(updated.getConnectorType());
        if (updated.getPricePerKwh() != null) charger.setPricePerKwh(updated.getPricePerKwh());
        if (updated.getStatus() != null) charger.setStatus(updated.getStatus());
        return chargerRepository.save(charger);
    }

    public void delete(Long id) {
        getById(id);
        chargerRepository.deleteById(id);
    }
}
