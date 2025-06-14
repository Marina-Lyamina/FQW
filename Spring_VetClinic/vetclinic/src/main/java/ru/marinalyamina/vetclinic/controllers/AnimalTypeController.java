package ru.marinalyamina.vetclinic.controllers;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.marinalyamina.vetclinic.models.entities.AnimalType;
import ru.marinalyamina.vetclinic.models.enums.Role;
import ru.marinalyamina.vetclinic.services.AnimalTypeService;
import ru.marinalyamina.vetclinic.services.EmployeeService;
import ru.marinalyamina.vetclinic.services.UserService;

import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/animaltypes")
public class AnimalTypeController {
    private final AnimalTypeService animalTypeService;
    private final UserService userService;
    private final EmployeeService employeeService;

    public AnimalTypeController(AnimalTypeService animalTypeService, UserService userService, EmployeeService employeeService) {
        this.animalTypeService = animalTypeService;
        this.userService = userService;
        this.employeeService = employeeService;
    }

    @GetMapping("/all")
    public String getAll(Model model) {
        model.addAttribute("animalTypes", animalTypeService.getAll());

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

        return "animaltypes/all";
    }

    @GetMapping("/details/{id}")
    public String details(Model model, @PathVariable("id") Long id) {
        Optional<AnimalType> optionalAnimalType = animalTypeService.getById(id);
        if(optionalAnimalType.isEmpty()){
            return "redirect:/animaltypes/all";
        }
        model.addAttribute("animalType", optionalAnimalType.get());
        return "animaltypes/details";
    }

    @GetMapping("/create")
    public String createGet(Model model) {
        model.addAttribute("animalType", new AnimalType());
        return "animaltypes/create";
    }

    @PostMapping("/create")
    public String createPost(@ModelAttribute("animalType") @Valid AnimalType animalType, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "animaltypes/create";
        }

        if (animalTypeService.existsByName(animalType.getName())) {
            bindingResult.rejectValue("name", "error.animalType", "Вид с таким названием уже существует");
            return "animaltypes/create";
        }

        animalTypeService.create(animalType);

        return "redirect:/animaltypes/all";
    }

    @GetMapping("/update/{id}")
    public String updateGet(Model model, @PathVariable("id") Long id) {
        Optional<AnimalType> optionalAnimalType = animalTypeService.getById(id);
        if (optionalAnimalType.isEmpty()) {
            return "redirect:/animaltypes/all";
        }
        model.addAttribute("animalType", optionalAnimalType.get());
        return "animaltypes/update";
    }

    @PostMapping("/update")
    public String updatePost(@ModelAttribute("animalType") @Valid AnimalType animalType, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "animaltypes/update";
        }

        Optional<AnimalType> existingAnimalTypeOpt = animalTypeService.getById(animalType.getId());
        if (existingAnimalTypeOpt.isEmpty()) {
            return "redirect:/animaltypes/all";
        }

        if (!Objects.equals(animalType.getName(), existingAnimalTypeOpt.get().getName()) && animalTypeService.existsByName(animalType.getName())) {
            bindingResult.rejectValue("name", "error.animalType", "Вид с таким названием уже существует");
            return "animaltypes/update";
        }

        animalTypeService.create(animalType);

        return "redirect:/animaltypes/all";
    }

    @GetMapping("/delete/{id}")
    public String deleteGet(Model model, @PathVariable("id") Long id) {
        Optional<AnimalType> optionalAnimalType = animalTypeService.getById(id);
        if(optionalAnimalType.isEmpty()){
            return "redirect:/animaltypes/all";
        }

        model.addAttribute("animalType", optionalAnimalType.get());
        model.addAttribute("hasAnimals", optionalAnimalType.get().getAnimals() != null && !optionalAnimalType.get().getAnimals().isEmpty());

        return "animaltypes/delete";
    }

    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable("id") Long id, Model model) {
        Optional<AnimalType> optionalAnimalType = animalTypeService.getById(id);
        if(optionalAnimalType.isEmpty()){
            return "redirect:/animaltypes/all";
        }

        AnimalType animalType = optionalAnimalType.get();
        if(animalType.getAnimals() != null && !animalType.getAnimals().isEmpty()){
            model.addAttribute("animalType", animalType);
            model.addAttribute("hasAnimals", true);
            return "animaltypes/delete";
        }

        if(animalTypeService.existsById(id)){
            animalTypeService.delete(id);
        }
        return "redirect:/animaltypes/all";
    }
}
