package ru.marinalyamina.vetclinic.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.marinalyamina.vetclinic.models.dtos.CreateAnimalDTO;
import ru.marinalyamina.vetclinic.models.dtos.UpdateAnimalDTO;
import ru.marinalyamina.vetclinic.models.entities.*;
import ru.marinalyamina.vetclinic.models.enums.Role;
import ru.marinalyamina.vetclinic.services.*;
import ru.marinalyamina.vetclinic.utils.FileManager;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/animals")
public class AnimalController {

    private final AnimalService animalService;
    private final ClientService clientService;
    private final AnimalTypeService animalTypeService;
    private final FileService fileService;
    private final UserService userService;
    private final EmployeeService employeeService;
    private final BreedService breedService;

    @Autowired
    public AnimalController(AnimalService animalService, ClientService clientService, AnimalTypeService animalTypeService, FileService fileService, UserService userService,EmployeeService employeeService, BreedService breedService) {
        this.animalService = animalService;
        this.clientService = clientService;
        this.animalTypeService = animalTypeService;
        this.fileService = fileService;
        this.userService = userService;
        this.employeeService = employeeService;
        this.breedService = breedService;
    }

    /*@GetMapping("/details/{id}")
    public String getAnimalDetails(@PathVariable Long id, Model model) {
        Optional<Animal> animalOptional = animalService.getById(id);
        if (animalOptional.isEmpty()) {
            return "redirect:/clients/all";
        }

        Animal animal = animalOptional.get();

        List<Schedule> futureSchedules = animal.getSchedules().stream()
                .filter(schedule -> schedule.getDate().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(Schedule::getDate))
                .toList();

        animal.setSchedules(futureSchedules);

        model.addAttribute("animal", animal);

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
            return "redirect:/clients/details" + animal.getClient().getId();
        }
        return "animals/details";
    }*/

    @GetMapping("/details/{id}")
    public String getAnimalDetails(@PathVariable Long id, Model model) {
        Optional<Animal> animalOptional = animalService.getById(id);
        if (animalOptional.isEmpty()) {
            return "redirect:/clients/all";
        }

        Animal animal = animalOptional.get();

        List<Schedule> futureSchedules = animal.getSchedules().stream()
                .filter(schedule -> schedule.getDate().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(Schedule::getDate))
                .toList();

        animal.setSchedules(futureSchedules);

        List<Appointment> sortedAppointments = animal.getAppointments().stream()
                .sorted((a1, a2) -> a2.getDate().compareTo(a1.getDate()))
                .toList();

        animal.setAppointments(sortedAppointments);

        model.addAttribute("animal", animal);

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
            return "redirect:/clients/details/" + animal.getClient().getId(); // Исправил путь
        }

        var currentUser = userService.getCurrentUser();

        if (currentUser.isPresent()) {
            var employee = employeeService.getAll().stream()
                    .filter(e -> e.getUser().getId().equals(currentUser.get().getId()))
                    .findFirst();

            if(employee.get().getUser().getRole() == Role.ROLE_ADMIN){
                model.addAttribute("isAdmin", true);
            }
            else{
                model.addAttribute("isAdmin", false);
            }
        }

        return "animals/details";
    }


   @GetMapping("/create")
    public String craeteGet(@RequestParam Long clientId, Model model) {
        Optional<Client> optionalClient = clientService.getById(clientId);
        if (optionalClient.isEmpty()) {
            return "redirect:/clients/all";
        }
        Client client = optionalClient.get();

        model.addAttribute("animal", new CreateAnimalDTO());
        model.addAttribute("client", client);
        model.addAttribute("animalTypes", animalTypeService.getAll());

        return "animals/create";
    }

    @PostMapping("/create")
    public String createPost(@RequestParam Long clientId, @ModelAttribute("animal") @Valid CreateAnimalDTO animalDTO, BindingResult bindingResult, Model model) {
        /*if (bindingResult.hasErrors()) {
            model.addAttribute("animal", animalDTO);
            model.addAttribute("client", clientService.getById(clientId).get());
            model.addAttribute("animalTypes", animalTypeService.getAll());
            return "animals/create";
        }

        Optional<Client> clientOptional = clientService.getById(clientId);
        if (clientOptional.isEmpty()) {
            return "redirect:/clients/all";
        }

        Animal animal = new Animal();
        animal.setName(animalDTO.getName());
        animal.setBirthday(animalDTO.getBirthday());
        animal.setGender(animalDTO.getGender());
        animal.setBreed(animalDTO.getBreed());

        Optional<AnimalType> animalType = animalTypeService.getById(animalDTO.getAnimalTypeId());
        animalType.ifPresent(animal::setAnimalType);

        animal.setClient(clientOptional.get());

        animalService.create(animal);

        return "redirect:/clients/details/" + clientId;*/
        return "";
    }


    @GetMapping("/update/{id}")
    public String updateGet(@PathVariable("id") Long id, Model model) {
        Optional<Animal> optionalAnimal = animalService.getById(id);
        if (optionalAnimal.isEmpty()) {
            return "redirect:/clients/all";
        }

        Animal animal = optionalAnimal.get();
        model.addAttribute("animal", animal);
        model.addAttribute("animalTypes", animalTypeService.getAll());
        model.addAttribute("client", animal.getClient());

        return "animals/update";
    }


    @PostMapping("/update")
    public String updatePost(@ModelAttribute("animal") @Valid UpdateAnimalDTO animalDTO,
                             BindingResult bindingResult,
                             Model model) {
        /*if (bindingResult.hasErrors()) {
            model.addAttribute("animalTypes", animalTypeService.getAll());

            Optional<Animal> optionalAnimal = animalService.getById(animalDTO.getId());
            optionalAnimal.ifPresent(animal -> model.addAttribute("client", animal.getClient()));

            return "animals/update";
        }

        Optional<Animal> optionalAnimal = animalService.getById(animalDTO.getId());
        if (optionalAnimal.isEmpty()) {
            return "redirect:/clients/all";
        }

        Animal animal = optionalAnimal.get();
        animal.setName(animalDTO.getName());
        animal.setBirthday(animalDTO.getBirthday());
        animal.setGender(animalDTO.getGender());
        animal.setBreed(animalDTO.getBreed());
        animal.setAnimalType(animalDTO.getAnimalType());

        animalService.updateAnimal(animal);

        return "redirect:/animals/details/" + animal.getId();*/
        return "";
    }



    @GetMapping("/delete/{id}")
    public String deleteGet(@PathVariable("id") Long id, Model model) {
        Optional<Animal> optionalAnimal = animalService.getById(id);
        if (optionalAnimal.isEmpty()) {
            return "redirect:/clients/all";
        }

        Animal animal = optionalAnimal.get();

        boolean hasAppointmentsOrSchedules = (animal.getAppointments() != null && !animal.getAppointments().isEmpty()) ||
                (animal.getSchedules() != null && !animal.getSchedules().isEmpty());

        model.addAttribute("hasAppointmentsOrSchedules", hasAppointmentsOrSchedules);
        model.addAttribute("animal", animal);
        return "animals/delete";
    }


    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable("id") Long id, Model model) {
        Optional<Animal> optionalAnimal = animalService.getById(id);
        if (optionalAnimal.isEmpty()) {
            return "redirect:/clients/all";
        }

        Animal animalToDelete = optionalAnimal.get();

        boolean hasAppointmentsOrSchedules = (animalToDelete.getAppointments() != null && !animalToDelete.getAppointments().isEmpty()) ||
                (animalToDelete.getSchedules() != null && !animalToDelete.getSchedules().isEmpty());

        if (hasAppointmentsOrSchedules) {
            model.addAttribute("animal", animalToDelete);
            model.addAttribute("hasAppointmentsOrSchedules", true);
            return "animals/delete";
        }

        animalService.delete(animalToDelete.getId());
        return "redirect:/clients/details/" + animalToDelete.getClient().getId();
    }

    @GetMapping("/updatePhoto/{id}")
    public String updatePhotoGet(@PathVariable("id") Long id, Model model) {
        Optional<Animal> optionalAnimal = animalService.getById(id);
        if (optionalAnimal.isEmpty()) {
            return "redirect:/animals/all";
        }

        model.addAttribute("animalId", id);

        return "animals/updatePhoto";
    }

    @PostMapping("/updatePhoto/{id}")
    public String updatePhotoPost(Model model, @PathVariable("id") Long id, @RequestParam("image") MultipartFile file) {
        Optional<Animal> optionalAnimal = animalService.getById(id);

        if (optionalAnimal.isEmpty()) {
            return "redirect:/animals/all";
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
                return "redirect:/animals/details/" + id;

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
            return "redirect:/animals/details/" + id;
        }

        fileService.update(dbFile);

        animal.setMainImage(dbFile);
        animalService.update(animal);

        return "redirect:/animals/details/" + id;
    }
}
