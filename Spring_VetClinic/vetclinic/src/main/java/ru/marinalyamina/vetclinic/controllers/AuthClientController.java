package ru.marinalyamina.vetclinic.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.marinalyamina.vetclinic.models.dtos.AnimalDTO;
import ru.marinalyamina.vetclinic.models.dtos.UpdateAnimalDTO;
import ru.marinalyamina.vetclinic.models.dtos.UpdatePetDTO;
import ru.marinalyamina.vetclinic.models.entities.*;
import ru.marinalyamina.vetclinic.models.enums.AnimalGender;
import ru.marinalyamina.vetclinic.services.*;
import ru.marinalyamina.vetclinic.utils.FileManager;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AuthClientController {

    private final UserService userService;
    private final ClientService clientService;
    private final AnimalService animalService;
    private final FileService fileService;
    private final AnimalTypeService animalTypeService;
    private final BreedService breedService;

    public AuthClientController(UserService userService, ClientService clientService, AnimalService animalService,
                                FileService fileService, AnimalTypeService animalTypeService, BreedService breedService) {
        this.userService = userService;
        this.clientService = clientService;
        this.animalService = animalService;
        this.fileService = fileService;
        this.animalTypeService = animalTypeService;
        this.breedService = breedService;
    }

    @GetMapping("/pets")
    public String pets(Model model) {
        var currentUser = userService.getCurrentUser();

        var client = clientService.getAll().stream()
                .filter(c -> c.getUser().getId().equals(currentUser.get().getId()))
                .findFirst();

        boolean hasAnimals = false;

        if (client.isPresent()) {
            var clientAnimals = client.get().getAnimals();
            if (!clientAnimals.isEmpty()) {
                hasAnimals = true;

                List<AnimalDTO> animalsWithPhoto = clientAnimals.stream().map(animal -> {
                    try {
                        String base64 = (animal.getMainImage() != null)
                                ? FileManager.getFile(animal.getMainImage().getName())
                                : FileManager.getBaseFile("defaultAnimal.jpg");

                        var dto = new AnimalDTO(animal, base64);

                        // Фильтруем schedules по дате после текущего момента
                        dto.setSchedules(
                                animal.getSchedules().stream()
                                        .filter(schedule -> schedule.getDate().isAfter(LocalDateTime.now()))
                                        .toList()
                        );

                        return dto;

                    } catch (Exception e) {
                        System.err.println("Ошибка при загрузке фото питомца: " + e.getMessage());
                        try {
                            return new AnimalDTO(animal, FileManager.getBaseFile("defaultAnimal.jpg"));
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }).toList();

                model.addAttribute("animals", animalsWithPhoto);
            }
        }

        model.addAttribute("hasAnimals", hasAnimals);
        return "auth_clients/pets";
    }

    @GetMapping("/pets/{id}")
    public String getAnimalDetails(@PathVariable Long id, Model model) throws JsonProcessingException {
        Optional<Animal> animalOptional = animalService.getById(id);
        if (animalOptional.isEmpty()) {
            return "redirect:/pets";
        }

        Animal animal = animalOptional.get();

        List<Schedule> futureSchedules = animal.getSchedules().stream()
                .filter(schedule -> schedule.getDate().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(Schedule::getDate))
                .toList();

        animal.setSchedules(futureSchedules);

        boolean hasFutureSchedules = true;
        if(futureSchedules.isEmpty()){
            hasFutureSchedules = false;
        }

        boolean hasAppointments = true;
        if(animal.getAppointments().isEmpty()){
            hasAppointments = false;
        }

        //
        List<Appointment> sortedAppointments = animal.getAppointments().stream()
                .sorted((a1, a2) -> a2.getDate().compareTo(a1.getDate()))
                .toList();

        animal.setAppointments(sortedAppointments);
        //

        model.addAttribute("animal", animal);
        model.addAttribute("hasFutureSchedules", hasFutureSchedules);
        model.addAttribute("hasAppointments", hasAppointments);

        //---------------------------------------------------------------------
        model.addAttribute("animalTypes", animalTypeService.getAll());

        Map<Long, List<Map<String, Object>>> breedMap = new HashMap<>();

        for (AnimalType type : animalTypeService.getAll()) {
            List<Map<String, Object>> breedDtos = breedService.getBreedsByTypeId(type.getId())
                    .stream()
                    .sorted(Comparator.comparing(Breed::getName))
                    .map(b -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", b.getId());
                        map.put("name", b.getName());
                        return map;
                    })
                    .collect(Collectors.toList());

            breedMap.put(type.getId(), breedDtos);
        }

        model.addAttribute("breedsJson", new ObjectMapper().writeValueAsString(breedMap));

        //----------------------------------------------------------------

        try{
            if(animal.getMainImage() == null){
                model.addAttribute("filePhoto", FileManager.getBaseFile("defaultAnimal.jpg"));
            }
            else{
                model.addAttribute("filePhoto", FileManager.getFile(animal.getMainImage().getName()));
            }
        }
        catch(Exception e){
            System.err.println("Ошибка при загрузке фото питомца: " + e.getMessage());
            return "redirect:/pets";
        }
        return "auth_clients/details_pet";
    }

//    @GetMapping("/pets/updatePhoto/{id}")
//    public String updatePhotoGet(@PathVariable("id") Long id, Model model) {
//        Optional<Animal> optionalAnimal = animalService.getById(id);
//        if (optionalAnimal.isEmpty()) {
//            return "redirect:/pets";
//        }
//
//        model.addAttribute("animalId", id);
//
//        return "auth_clients/updatePhoto";
//    }

    @PostMapping("/pets/updatePhoto/{id}")
    public String updateAnimalPhotoPost(Model model, @PathVariable("id") Long id, @RequestParam("image") MultipartFile file) {
        Optional<Animal> optionalAnimal = animalService.getById(id);

        if (optionalAnimal.isEmpty()) {
            return "redirect:/pets";
        }

        Animal animal = optionalAnimal.get();
        DbFile dbFile = animal.getMainImage();

        if (dbFile == null){
            dbFile = new DbFile();
        }
        else {
            try {
                FileManager.deleteFile(dbFile.getName());
            } catch (IOException e) {
                return "redirect:/pets/" + id;

            }
        }

        String fileName = file.getOriginalFilename();
        String extension = ".jpg";
        int index = fileName.lastIndexOf('.');
        if (index > 0 && index < fileName.length() - 1) {
            extension = fileName.substring(index + 1);
        }

        dbFile.setName(FileManager.createFileName(extension));
        dbFile.setDate(LocalDateTime.now());

        try {
            FileManager.saveFile(dbFile.getName(), file.getBytes());
        } catch (IOException e) {
            return "redirect:/pets/" + id;
        }

        fileService.update(dbFile);

        animal.setMainImage(dbFile);
        animalService.update(animal);

        return "redirect:/pets/" + id;
    }

    /*@GetMapping("/pets/update/{id}")
    public String updateAnimalGet(@PathVariable("id") Long id, Model model) throws JsonProcessingException {
        Optional<Animal> optionalAnimal = animalService.getById(id);
        if (optionalAnimal.isEmpty()) {
            return "redirect:/pets";
        }

        Animal animal = optionalAnimal.get();
        model.addAttribute("animal", animal);
        model.addAttribute("animalTypes", animalTypeService.getAll());

        Map<Long, List<Map<String, Object>>> breedMap = new HashMap<>();

        for (AnimalType type : animalTypeService.getAll()) {
            List<Map<String, Object>> breedDtos = breedService.getBreedsByTypeId(type.getId())
                    .stream()
                    .sorted(Comparator.comparing(Breed::getName))
                    .map(b -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", b.getId());
                        map.put("name", b.getName());
                        return map;
                    })
                    .collect(Collectors.toList());

            breedMap.put(type.getId(), breedDtos);
        }

        model.addAttribute("breedsJson", new ObjectMapper().writeValueAsString(breedMap));

        //System.out.println(new ObjectMapper().writeValueAsString(breedMap));

        return "auth_clients/update_pet";
    }*/


    /*@PostMapping("/pets/update")
    public String updatePost(@ModelAttribute("animal") @Valid UpdateAnimalDTO animalDTO,
                             BindingResult bindingResult,
                             Model model) throws JsonProcessingException {

        if (bindingResult.hasErrors()) {

            model.addAttribute("animalTypes", animalTypeService.getAll());

            Map<Long, List<Map<String, Object>>> breedMap = new HashMap<>();

            for (AnimalType type : animalTypeService.getAll()) {
                List<Map<String, Object>> breedDtos = breedService.getBreedsByTypeId(type.getId())
                        .stream()
                        .sorted(Comparator.comparing(Breed::getName))
                        .map(b -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("id", b.getId());
                            map.put("name", b.getName());
                            return map;
                        })
                        .collect(Collectors.toList());

                breedMap.put(type.getId(), breedDtos);
            }

            model.addAttribute("breedsJson", new ObjectMapper().writeValueAsString(breedMap));

            return "auth_clients/update_pet";
        }

        Optional<Animal> optionalAnimal = animalService.getById(animalDTO.getId());
        if (optionalAnimal.isEmpty()) {
            return "redirect:/pets";
        }

        Animal animal = optionalAnimal.get();
        animal.setName(animalDTO.getName());
        animal.setBirthday(animalDTO.getBirthday());
        animal.setGender(animalDTO.getGender());
        animal.setBreed(animalDTO.getBreed());
        animal.setAnimalType(animalDTO.getAnimalType());

        animalService.updateAnimal(animal);

        return "redirect:/pets/" + animal.getId();
    }*/

    /*@PostMapping("/pets/update")
    public String updatePost(@ModelAttribute("animal") @Valid UpdateAnimalDTO animalDTO,
                             BindingResult bindingResult,
                             Model model) throws IOException {

        Optional<Animal> optionalAnimal = animalService.getById(animalDTO.getId());
        if (optionalAnimal.isEmpty()) {
            return "redirect:/pets";
        }

        Animal animal = optionalAnimal.get();

        if (!bindingResult.hasErrors()) {
            System.out.println("Ошибок нет, имя: " + animalDTO.getName());
        }

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(System.out::println);

            model.addAttribute("animalTypes", animalTypeService.getAll());

            Map<Long, List<Map<String, Object>>> breedMap = new HashMap<>();
            for (AnimalType type : animalTypeService.getAll()) {
                List<Map<String, Object>> breedDtos = breedService.getBreedsByTypeId(type.getId())
                        .stream()
                        .sorted(Comparator.comparing(Breed::getName))
                        .map(b -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("id", b.getId());
                            map.put("name", b.getName());
                            return map;
                        })
                        .collect(Collectors.toList());
                breedMap.put(type.getId(), breedDtos);
            }

            model.addAttribute("breedsJson", new ObjectMapper().writeValueAsString(breedMap));

            try {
                if (animal.getMainImage() == null) {
                    model.addAttribute("filePhoto", FileManager.getBaseFile("defaultAnimal.jpg"));
                } else {
                    model.addAttribute("filePhoto", FileManager.getFile(animal.getMainImage().getName()));
                }
            } catch (IOException e) {
                System.err.println("Ошибка при загрузке фото питомца: " + e.getMessage());
                model.addAttribute("filePhoto", FileManager.getBaseFile("defaultAnimal.jpg"));
            }

            model.addAttribute("animal", animalDTO);

            model.addAttribute("showUpdateModal", true);

            return "auth_clients/details_pet";
        }

        animal.setName(animalDTO.getName());
        animal.setBirthday(animalDTO.getBirthday());
        animal.setGender(animalDTO.getGender());
        animal.setBreed(animalDTO.getBreed());
        animal.setAnimalType(animalDTO.getAnimalType());

        animalService.updateAnimal(animal);

        return "redirect:/pets/" + animal.getId();
    }*/

    @PostMapping(value = "/pets/update", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> updatePost(@RequestBody @Valid UpdatePetDTO animalDTO,
                                        BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(e ->
                    errors.put(e.getField(), e.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        Optional<Animal> optionalAnimal = animalService.getById(animalDTO.getId());
        if (optionalAnimal.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Питомец не найден"));
        }

        Animal animal = optionalAnimal.get();
        animal.setName(animalDTO.getName());
        animal.setBirthday(animalDTO.getBirthday());
        animal.setGender(animalDTO.getGender());

        if (animalDTO.getBreedId() != null) {
            breedService.getById(animalDTO.getBreedId()).ifPresent(animal::setBreed);
        } else {
            animal.setBreed(null);
        }

        if (animalDTO.getAnimalTypeId() != null) {
            animalTypeService.getById(animalDTO.getAnimalTypeId()).ifPresent(animal::setAnimalType);
        } else {
            animal.setAnimalType(null);
        }

        animalService.updateAnimal(animal);

        return ResponseEntity.ok(Map.of("id", animal.getId()));
    }

    @GetMapping("/pets/create")
    public String createForm(Model model) throws JsonProcessingException {
        Animal animal = new Animal();
        animal.setGender(AnimalGender.Other); // по умолчанию
        model.addAttribute("animal", animal);
        model.addAttribute("animalTypes", animalTypeService.getAll());
        Map<Long, List<Map<String, Object>>> breedMap = new HashMap<>();
        for (AnimalType type : animalTypeService.getAll()) {
            List<Map<String, Object>> breedDtos = breedService.getBreedsByTypeId(type.getId())
                    .stream()
                    .sorted(Comparator.comparing(Breed::getName))
                    .map(b -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", b.getId());
                        map.put("name", b.getName());
                        return map;
                    })
                    .collect(Collectors.toList());

            breedMap.put(type.getId(), breedDtos);
        }
        model.addAttribute("breedsJson", new ObjectMapper().writeValueAsString(breedMap));

        return "auth_clients/create_pet";
    }

    @PostMapping("/pets/create")
    public String createAnimal(@ModelAttribute("animal") @Valid Animal animal,
                               BindingResult bindingResult,
                               Model model) throws JsonProcessingException {

        if (bindingResult.hasErrors()) {
            model.addAttribute("animalTypes", animalTypeService.getAll());

            Map<Long, List<Map<String, Object>>> breedMap = new HashMap<>();
            for (AnimalType type : animalTypeService.getAll()) {
                List<Map<String, Object>> breedDtos = breedService.getBreedsByTypeId(type.getId())
                        .stream()
                        .sorted(Comparator.comparing(Breed::getName))
                        .map(b -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("id", b.getId());
                            map.put("name", b.getName());
                            return map;
                        })
                        .collect(Collectors.toList());
                breedMap.put(type.getId(), breedDtos);
            }
            model.addAttribute("breedsJson", new ObjectMapper().writeValueAsString(breedMap));
            return "auth_clients/create_pet";
        }

        var currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }

        Optional<Client> optionalClient = clientService.getAll().stream()
                .filter(c -> c.getUser().getId().equals(currentUser.get().getId()))
                .findFirst();

        if (optionalClient.isEmpty()) {
            return "redirect:/pets";
        }

        animal.setClient(optionalClient.get());
        //save
        animalService.update(animal);

        return "redirect:/pets";
    }



}
