package ru.marinalyamina.vetclinic.models.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateAnimalScheduleDTO {

    private Long scheduleId;

    @NotNull(message = "Питомец должен быть выбран")
    private Long animalId;
}
