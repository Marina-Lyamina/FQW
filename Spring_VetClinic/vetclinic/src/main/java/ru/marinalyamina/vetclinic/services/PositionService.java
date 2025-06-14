package ru.marinalyamina.vetclinic.services;

import org.springframework.stereotype.Service;

import ru.marinalyamina.vetclinic.models.entities.Position;
import ru.marinalyamina.vetclinic.repositories.PositionRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PositionService {
    private final PositionRepository positionRepository;

    public PositionService(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    public List<Position> getAll() { return positionRepository.findAll();}

    public Optional<Position> getById(Long id) {
        return positionRepository.findById(id);
    }

    public boolean existsById(Long id) {
        return positionRepository.existsById(id);
    }

    public boolean existsByName(String name) {
        return positionRepository.existsByName(name);
    }

    public Position create(Position position) {
        return positionRepository.save(position);
    }

    public void delete(Long id) {
        positionRepository.deleteById(id);
    }

    public List<Position> findFiltered(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return positionRepository.findAll();
        }
        return positionRepository.findByNameContainingIgnoreCase(keyword);
    }
}
