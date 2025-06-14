package ru.marinalyamina.vetclinic.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.marinalyamina.vetclinic.models.entities.Breed;

import java.util.List;

@Repository
public interface BreedRepository extends JpaRepository<Breed, Long> {
    boolean existsByName(String name);
    List<Breed> findByAnimalTypeId(Long animalTypeId);

    List<Breed> findByNameContainingIgnoreCase(String keyword);

    List<Breed> findByAnimalTypeIdAndNameContainingIgnoreCase(Long animalTypeId, String keyword);

    boolean existsByNameIgnoreCaseAndAnimalTypeId(String name, Long animalTypeId);

}

