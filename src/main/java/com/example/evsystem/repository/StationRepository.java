package com.example.evsystem.repository;

import com.example.evsystem.entity.Station;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StationRepository extends JpaRepository<Station, Long> {
    boolean existsByNameIgnoreCase(String name);
    Optional<Station> findByNameIgnoreCase(String name);

    @Override
    @EntityGraph(attributePaths = {"chargers"})
    List<Station> findAll();

    @Override
    @EntityGraph(attributePaths = {"chargers"})
    Optional<Station> findById(Long id);
}
