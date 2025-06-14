package ru.marinalyamina.vetclinic.services;

import org.springframework.stereotype.Service;
import ru.marinalyamina.vetclinic.models.entities.Breed;
import ru.marinalyamina.vetclinic.repositories.AnimalRepository;
import ru.marinalyamina.vetclinic.repositories.BreedRepository;

import java.util.List;
import java.util.Optional;

@Service
public class BreedService {
    private final BreedRepository breedRepository;
    private final AnimalRepository animalRepository;

    public BreedService(BreedRepository breedRepository, AnimalRepository animalRepository) {
        this.breedRepository = breedRepository;
        this.animalRepository = animalRepository;    }

    public List<Breed> getAll() { return breedRepository.findAll();}

    public Optional<Breed> getById(Long id) {
        return breedRepository.findById(id);
    }

    public boolean existsById(Long id) {
        return breedRepository.existsById(id);
    }

    public boolean existsByName(String name) {
        return breedRepository.existsByName(name);
    }

    public Breed create(Breed breed) {
        return breedRepository.save(breed);
    }

    public void delete(Long id) {
        breedRepository.deleteById(id);
    }

    public List<Breed> getBreedsByTypeId(Long typeId) {
        return breedRepository.findByAnimalTypeId(typeId);
    }

    public List<Breed> findFiltered(Long animalTypeId, String keyword) {
        if (animalTypeId != null && keyword != null && !keyword.isBlank()) {
            return breedRepository.findByAnimalTypeIdAndNameContainingIgnoreCase(animalTypeId, keyword);
        } else if (animalTypeId != null) {
            return breedRepository.findByAnimalTypeId(animalTypeId);
        } else if (keyword != null && !keyword.isBlank()) {
            return breedRepository.findByNameContainingIgnoreCase(keyword);
        } else {
            return getAll();
        }
    }

    public boolean existsByNameAndAnimalTypeId(String name, Long animalTypeId) {
        return breedRepository.existsByNameIgnoreCaseAndAnimalTypeId(name, animalTypeId);
    }



    public boolean hasAnimals(Long breedId) {
        return animalRepository.existsByBreedId(breedId);
    }


}
