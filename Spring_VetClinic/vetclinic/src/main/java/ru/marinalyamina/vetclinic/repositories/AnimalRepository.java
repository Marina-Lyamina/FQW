package ru.marinalyamina.vetclinic.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.marinalyamina.vetclinic.models.entities.Animal;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, Long> {
    List<Animal> findByClient_Id(Long clientId);

    @Query("SELECT a FROM Animal a LEFT JOIN FETCH a.schedules WHERE a.id = :id")
    Optional<Animal> findWithSchedulesById(Long id);

    Boolean existsByBreedId(Long breedId);
}
