package com.example.evsystem.repository;

import com.example.evsystem.entity.Charger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface ChargerRepository extends JpaRepository<Charger, Long> {
    boolean existsByChargerCodeIgnoreCase(String chargerCode);
    boolean existsByChargerCodeIgnoreCaseAndIdNot(String chargerCode, Long id);
    List<Charger> findByStationId(Long stationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Charger c where c.id = :id")
    Optional<Charger> findByIdForUpdate(@Param("id") Long id);
}
