package ru.marinalyamina.vetclinic.services;

import org.springframework.stereotype.Service;

import ru.marinalyamina.vetclinic.models.entities.Animal;
import ru.marinalyamina.vetclinic.models.entities.Schedule;
import ru.marinalyamina.vetclinic.repositories.AnimalRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AnimalService {

    private final AnimalRepository animalRepository;

    public AnimalService(AnimalRepository animalRepository) {
        this.animalRepository = animalRepository;
    }

    public List<Animal> getAll() {
        return animalRepository.findAll();
    }

    public List<Animal> getAnimalsForClient(Long clientId) {
        List<Animal> animals = animalRepository.findByClient_Id(clientId);

        // Оставляем только будущие записи на приемы
        for (Animal animal : animals) {
            List<Schedule> upcomingSchedules = animal.getSchedules().stream()
                    .filter(schedule -> schedule.getDate().isAfter(LocalDateTime.now()))
                    .collect(Collectors.toList());
            animal.setSchedules(upcomingSchedules);
        }

        return animals;
    }

    public Optional<Animal> getById(Long id) {
        Optional<Animal> animalOptional = animalRepository.findById(id);

        if (animalOptional.isEmpty()) {
            return animalOptional;
        }

        Animal animal = animalOptional.get();

        // Оставляем только будущие записи на приемы
        List<Schedule> upcomingSchedules = animal.getSchedules().stream()
                .filter(schedule -> schedule.getDate().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());
        animal.setSchedules(upcomingSchedules);

        return Optional.of(animal);
    }

    public boolean existsById(Long id) {
        return animalRepository.existsById(id);
    }

    public Animal create(Animal animal) {
        /*if (animal.getBreed().isBlank()) {
            animal.setBreed(null);
        }*/
        return animalRepository.save(animal);
    }

    public Animal update(Animal animal) {
        return animalRepository.save(animal);
    }

    public void updateAnimal(Animal animal) {
        /*if (animal.getBreed().isBlank()) {
            animal.setBreed(null);
        }*/
        animalRepository.save(animal);
    }

    public void delete(Long id) {
        animalRepository.deleteById(id);
    }

    public Optional<Animal> findWithSchedulesById(Long id) {
        return animalRepository.findWithSchedulesById(id);
    }


}