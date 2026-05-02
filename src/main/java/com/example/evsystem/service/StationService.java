package com.example.evsystem.service;

import com.example.evsystem.dto.StationDiscoveryResponse;
import com.example.evsystem.dto.StationRequest;
import com.example.evsystem.dto.StationUpdateRequest;
import com.example.evsystem.entity.Station;
import com.example.evsystem.enums.ConnectorType;
import com.example.evsystem.enums.PowerOutput;
import com.example.evsystem.enums.StationStatus;
import com.example.evsystem.exception.BusinessException;
import com.example.evsystem.repository.ChargerRepository;
import com.example.evsystem.repository.ChargingSessionRepository;
import com.example.evsystem.repository.ReservationRepository;
import com.example.evsystem.repository.StationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class StationService {

    private final StationRepository stationRepository;
    private final ChargerRepository chargerRepository;
    private final ReservationRepository reservationRepository;
    private final ChargingSessionRepository chargingSessionRepository;

    public StationService(StationRepository stationRepository,
                          ChargerRepository chargerRepository,
                          ReservationRepository reservationRepository,
                          ChargingSessionRepository chargingSessionRepository) {
        this.stationRepository = stationRepository;
        this.chargerRepository = chargerRepository;
        this.reservationRepository = reservationRepository;
        this.chargingSessionRepository = chargingSessionRepository;
    }

    public Station save(StationRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Station name cannot be empty");
        }
        if (stationRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Station with this name already exists");
        }

        Station station = new Station();
        station.setName(request.getName().trim());
        station.setAddress(request.getAddress().trim());
        station.setLatitude(request.getLatitude());
        station.setLongitude(request.getLongitude());
        station.setStatus(request.getStatus());
        return stationRepository.save(station);
    }

    public List<Station> getAll() {
        return stationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<StationDiscoveryResponse> discover(
            ConnectorType connectorType,
            PowerOutput powerOutput,
            Float maxPricePerKwh,
            StationStatus status,
            Double userLatitude,
            Double userLongitude
    ) {
        return stationRepository.findAll().stream()
                .map(station -> StationDiscoveryResponse.from(station, userLatitude, userLongitude))
                .filter(station -> connectorType == null || station.getConnectorTypes().contains(connectorType))
                .filter(station -> powerOutput == null || station.getPowerOutputs().contains(powerOutput))
                .filter(station -> maxPricePerKwh == null || (station.getMinPricePerKwh() != null && station.getMinPricePerKwh() <= maxPricePerKwh))
                .filter(station -> status == null || station.getStatus() == status)
                .sorted(userLatitude != null && userLongitude != null
                        ? Comparator.comparing(StationDiscoveryResponse::getDistanceKm, Comparator.nullsLast(Double::compareTo))
                        : Comparator.comparing(StationDiscoveryResponse::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Station getById(Long id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Station not found with id: " + id));
    }

    public Station update(Long id, StationUpdateRequest updated) {
        Station station = getById(id);
        if (updated.getName() != null) {
            if (updated.getName().isBlank()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Station name cannot be empty");
            }
            stationRepository.findByNameIgnoreCase(updated.getName())
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new BusinessException(HttpStatus.CONFLICT, "Station with this name already exists");
                    });
            station.setName(updated.getName().trim());
        }
        if (updated.getAddress() != null) {
            if (updated.getAddress().isBlank()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Address cannot be empty");
            }
            station.setAddress(updated.getAddress().trim());
        }
        if (updated.getLatitude() != null) station.setLatitude(updated.getLatitude());
        if (updated.getLongitude() != null) station.setLongitude(updated.getLongitude());
        if (updated.getStatus() != null) station.setStatus(updated.getStatus());
        return stationRepository.save(station);
    }

    public void delete(Long id) {
        getById(id);
        if (reservationRepository.existsByChargerStationId(id) ||
                chargingSessionRepository.existsByReservation_Charger_Station_Id(id) ||
                !chargerRepository.findByStationId(id).isEmpty()) {
            throw new BusinessException(HttpStatus.CONFLICT, "Station with chargers, reservations, or charging session history cannot be deleted.");
        }
        stationRepository.deleteById(id);
    }
}
