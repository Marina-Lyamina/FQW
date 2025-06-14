package ru.marinalyamina.vetclinic.models.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateScheduleForAnimalChooseEmployeeDTO {
    @NotNull(message = "Выберите питомца")
    private Long animalId;

    @NotNull(message = "Выберите сотрудника")
    private Long employeeId;
}
