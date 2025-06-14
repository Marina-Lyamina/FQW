package ru.marinalyamina.vetclinic.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.marinalyamina.vetclinic.models.entities.Position;

import java.util.List;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    boolean existsByName(String name);

    List<Position>  findByNameContainingIgnoreCase(String name);
}
