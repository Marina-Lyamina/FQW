package ru.marinalyamina.vetclinic.models.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import ru.marinalyamina.vetclinic.models.entities.AnimalType;
import ru.marinalyamina.vetclinic.models.entities.Breed;
import ru.marinalyamina.vetclinic.models.enums.AnimalGender;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateAnimalDTO {
    private Long id;

    @NotEmpty(message = "Введите кличку")
    //@NotBlank(message = "Введите кличку")
    @Size(max = 64, message = "Слишком длинная кличка")
    private String name;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, fallbackPatterns = {"dd.MM.yyyy"})
    private LocalDate birthday;

    private AnimalGender gender;

    /*@Size(max = 64, message = "Слишком длинное название породы")
    private String breed;*/
    private Breed breed;

    private AnimalType animalType;
}
