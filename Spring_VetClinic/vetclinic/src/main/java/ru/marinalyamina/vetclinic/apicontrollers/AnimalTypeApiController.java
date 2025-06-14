package ru.marinalyamina.vetclinic.apicontrollers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.marinalyamina.vetclinic.models.entities.AnimalType;
import ru.marinalyamina.vetclinic.services.AnimalTypeService;

import java.util.List;

@RestController
@RequestMapping("/api/animaltypes")
public class AnimalTypeApiController {
    private final AnimalTypeService animalTypeService;

    public AnimalTypeApiController(AnimalTypeService animalTypeService) {
        this.animalTypeService = animalTypeService;
    }

    @GetMapping()
    public List<AnimalType> getAll() {
        return animalTypeService.getAll();
    }
}
