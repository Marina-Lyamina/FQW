package ru.marinalyamina.vetclinic.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.marinalyamina.vetclinic.models.entities.Diagnosis;

import java.util.List;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {
    boolean existsByName(String name);
    boolean existsByNameIgnoreCase(String name);
    List<Diagnosis> findByNameContainingIgnoreCase(String name);
}
