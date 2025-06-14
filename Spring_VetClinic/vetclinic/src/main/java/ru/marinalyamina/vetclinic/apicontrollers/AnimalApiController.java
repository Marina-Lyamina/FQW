package ru.marinalyamina.vetclinic.apicontrollers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.marinalyamina.vetclinic.models.dtos.CreateAnimalDTO;
import ru.marinalyamina.vetclinic.models.dtos.CreateFileDTO;
import ru.marinalyamina.vetclinic.models.entities.Animal;
import ru.marinalyamina.vetclinic.models.entities.AnimalType;
import ru.marinalyamina.vetclinic.models.entities.Client;
import ru.marinalyamina.vetclinic.models.entities.DbFile;
import ru.marinalyamina.vetclinic.services.AnimalService;
import ru.marinalyamina.vetclinic.services.AnimalTypeService;
import ru.marinalyamina.vetclinic.services.ClientService;
import ru.marinalyamina.vetclinic.services.FileService;
import ru.marinalyamina.vetclinic.utils.CurrentUser;
import ru.marinalyamina.vetclinic.utils.FileManager;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/animals")
public class AnimalApiController {
    private final AnimalService animalService;
    private final AnimalTypeService animalTypeService;
    private final ClientService clientService;
    private final FileService fileService;

    public AnimalApiController(AnimalService animalService, ClientService clientService,
                               AnimalTypeService animalTypeService, FileService fileService) {
        this.animalService = animalService;
        this.animalTypeService = animalTypeService;
        this.clientService = clientService;
        this.fileService = fileService;
    }

    @GetMapping("/currentUser")
    public ResponseEntity<List<Animal>> getAnimalsForCurrentUser() {
        Long currentClientId = CurrentUser.clientId;

        List<Animal> animals = animalService.getAnimalsForClient(currentClientId);

        if (animals.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        try{
           for(var animal : animals){
               animal.initFiles();
           }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok(animals);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Animal> getAnimalById(@PathVariable Long id) {
        Optional<Animal> animalOptional = animalService.getById(id);

        if (animalOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try{
            animalOptional.get().initFiles();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok(animalOptional.get());
    }

    @PostMapping()
    public ResponseEntity<Animal> createAnimal(@RequestBody CreateAnimalDTO animalDTO) {
        Long currentClientId = CurrentUser.clientId;

        Optional<Client> clientOptional = clientService.getById(currentClientId);
        if (clientOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Optional<AnimalType> animalTypeOptional = animalTypeService.getById(animalDTO.getAnimalTypeId());
        if (animalTypeOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var animal = new Animal();

        animal.setName(animalDTO.getName());
        animal.setBirthday(animalDTO.getBirthday());
        animal.setGender(animalDTO.getGender());
        //animal.setBreed(animalDTO.getBreed());

        animal.setClient(clientOptional.get());
        animal.setAnimalType(animalTypeOptional.get());

        Animal createdAnimal = animalService.create(animal);

        return ResponseEntity.ok(createdAnimal);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Animal> updateAnimal(@PathVariable Long id, @RequestBody CreateAnimalDTO animalDTO) {
        Optional<Animal> animalOptional = animalService.getById(id);

        if (animalOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Animal animal = animalOptional.get();

        animal.setName(animalDTO.getName());
        animal.setBirthday(animalDTO.getBirthday());
        animal.setGender(animalDTO.getGender());
        //animal.setBreed(animalDTO.getBreed());

        Animal savedAnimal = animalService.update(animal);

        return ResponseEntity.ok(savedAnimal);
    }

    @PutMapping("/{id}/image")
    public ResponseEntity updateAnimalMainImage(@PathVariable Long id, @RequestBody CreateFileDTO fileDTO) {
        Optional<Animal> animalOptional = animalService.getById(id);

        if (animalOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Animal animal = animalOptional.get();
        DbFile dbFile = animal.getMainImage();

        if (dbFile == null){
            dbFile = new DbFile();
        }
        else {
            try {
                FileManager.deleteFile(dbFile.getName());
            } catch (IOException e) {
                return ResponseEntity.internalServerError().build();
            }
        }

        dbFile.setName(FileManager.createFileName(fileDTO.getFileExtensions()));
        dbFile.setDate(LocalDateTime.now());

        try {
            FileManager.saveFile(dbFile.getName(), fileDTO.getFileContent());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }

        fileService.update(dbFile);

        animal.setMainImage(dbFile);
        Animal savedAnimal = animalService.update(animal);

        return ResponseEntity.ok(savedAnimal);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteAnimal(@PathVariable Long id) {
        Optional<Animal> animalOptional = animalService.getById(id);

        if (animalOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!animalOptional.get().getAppointments().isEmpty() || !animalOptional.get().getSchedules().isEmpty()){
            return ResponseEntity.badRequest().build();
        }

        animalService.delete(id);

        return ResponseEntity.ok().build();
    }
}
