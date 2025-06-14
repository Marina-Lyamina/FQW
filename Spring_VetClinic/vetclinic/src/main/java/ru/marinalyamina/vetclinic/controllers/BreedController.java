package ru.marinalyamina.vetclinic.controllers;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.marinalyamina.vetclinic.models.entities.AnimalType;
import ru.marinalyamina.vetclinic.models.entities.Breed;
import ru.marinalyamina.vetclinic.models.enums.Role;
import ru.marinalyamina.vetclinic.services.AnimalTypeService;
import ru.marinalyamina.vetclinic.services.BreedService;
import ru.marinalyamina.vetclinic.services.EmployeeService;
import ru.marinalyamina.vetclinic.services.UserService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/breeds")
public class BreedController {
    private final BreedService breedService;
    private final AnimalTypeService animalTypeService;
    private final UserService userService;
    private final EmployeeService employeeService;

    public BreedController(BreedService breedService, AnimalTypeService animalTypeService, UserService userService,
                           EmployeeService employeeService) {
        this.breedService = breedService;
        this.animalTypeService = animalTypeService;
        this.userService = userService;
        this.employeeService = employeeService;
    }

    @GetMapping("/all")
    public String getAll(@RequestParam(value = "animalTypeId", required = false) Long animalTypeId,
                         @RequestParam(value = "keyword", required = false) String keyword,
                         Model model) {
        List<Breed> breeds = breedService.findFiltered(animalTypeId, keyword);
        List<AnimalType> animalTypes = animalTypeService.getAll();

        model.addAttribute("breeds", breeds);
        model.addAttribute("animalTypes", animalTypes);
        model.addAttribute("selectedTypeId", animalTypeId);
        model.addAttribute("keyword", keyword);

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

        return "breeds/all";
    }

    @GetMapping("/table")
    public String getBreedTableFragment(@RequestParam(required = false) Long animalTypeId,
                                        @RequestParam(required = false) String keyword,
                                        Model model) {
        List<Breed> breeds = breedService.findFiltered(animalTypeId, keyword);
        model.addAttribute("breeds", breeds);
        return "breeds/all :: tableContent";
    }

    @GetMapping("/details/{id}")
    public String details(Model model, @PathVariable("id") Long id) {
        Optional<Breed> optionalBreed = breedService.getById(id);
        if (optionalBreed.isEmpty()) {
            return "redirect:/breeds/all";
        }
        model.addAttribute("breed", optionalBreed.get());
        return "breeds/details";
    }

    @GetMapping("/create")
    public String createGet(Model model) {
        model.addAttribute("breed", new Breed());
        model.addAttribute("animalTypes", animalTypeService.getAll());
        return "breeds/create";
    }

    @PostMapping("/create")
    public String createPost(@ModelAttribute("breed") @Valid Breed breed,
                             BindingResult bindingResult, Model model) {
        if (breedService.existsByNameAndAnimalTypeId(breed.getName(), breed.getAnimalType().getId())) {
            bindingResult.rejectValue("name", "error.breed", "Такая порода уже существует для этого вида");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("animalTypes", animalTypeService.getAll());
            return "breeds/create";
        }

        breedService.create(breed);
        return "redirect:/breeds/all";
    }

    @GetMapping("/update/{id}")
    public String updateGet(@PathVariable("id") Long id, Model model) {
        Optional<Breed> optionalBreed = breedService.getById(id);
        if (optionalBreed.isEmpty()) {
            return "redirect:/breeds/all";
        }
        model.addAttribute("breed", optionalBreed.get());
        return "breeds/update";
    }

    @PostMapping("/update")
    public String updatePost(@ModelAttribute("breed") @Valid Breed breed,
                             BindingResult bindingResult) {

        Optional<Breed> existing = breedService.getById(breed.getId());
        if (existing.isEmpty()) {
            return "redirect:/breeds/all";
        }

        Breed current = existing.get();

        if (!current.getName().equals(breed.getName())) {
            if (breedService.existsByNameAndAnimalTypeId(breed.getName(), current.getAnimalType().getId())) {
                bindingResult.rejectValue("name", "error.breed", "Такая порода уже существует для этого вида");
            }
        }

        if (bindingResult.hasErrors()) {
            breed.setAnimalType(current.getAnimalType()); // чтобы заново показать в форме
            return "breeds/update";
        }

        current.setName(breed.getName()); // обновляем только имя
        breedService.create(current); // сохраняем

        return "redirect:/breeds/all";
    }

    @GetMapping("/delete/{id}")
    public String deleteGet(Model model, @PathVariable("id") Long id) {
        Optional<Breed> optionalBreed = breedService.getById(id);
        if (optionalBreed.isEmpty()) {
            return "redirect:/breeds/all";
        }

        Breed breed = optionalBreed.get();
        boolean hasAnimals = breedService.hasAnimals(breed.getId());

        model.addAttribute("breed", breed);
        model.addAttribute("hasAnimals", hasAnimals);

        return "breeds/delete";
    }

    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable("id") Long id) {
        if (breedService.existsById(id) && !breedService.hasAnimals(id)) {
            breedService.delete(id);
        }
        return "redirect:/breeds/all";
    }
}
