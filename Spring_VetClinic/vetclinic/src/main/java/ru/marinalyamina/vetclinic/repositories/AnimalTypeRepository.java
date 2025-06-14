package ru.marinalyamina.vetclinic.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.marinalyamina.vetclinic.models.entities.AnimalType;

@Repository
public interface AnimalTypeRepository extends JpaRepository<AnimalType, Long> {
    boolean existsByName(String name);
}
