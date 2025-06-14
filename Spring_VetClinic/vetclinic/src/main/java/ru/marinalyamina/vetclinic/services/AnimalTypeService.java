package ru.marinalyamina.vetclinic.services;

import org.springframework.stereotype.Service;

import ru.marinalyamina.vetclinic.models.entities.AnimalType;
import ru.marinalyamina.vetclinic.repositories.AnimalTypeRepository;

import java.util.List;
import java.util.Optional;

@Service
public class AnimalTypeService {
    private final AnimalTypeRepository animalTypeRepository;

    public AnimalTypeService(AnimalTypeRepository animalTypeRepository) {this.animalTypeRepository = animalTypeRepository; }

    public List<AnimalType> getAll() { return animalTypeRepository.findAll();}

    public Optional<AnimalType> getById(Long id) {
        return animalTypeRepository.findById(id);
    }

    public boolean existsById(Long id) {
        return animalTypeRepository.existsById(id);
    }

    public boolean existsByName(String name) {
        return animalTypeRepository.existsByName(name);
    }

    public AnimalType create(AnimalType animalType) {
        return animalTypeRepository.save(animalType);
    }

    public void delete(Long id) {
        animalTypeRepository.deleteById(id);
    }
}
