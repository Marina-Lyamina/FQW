package ru.marinalyamina.vetclinic.models.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class CreateAppointmentChooseEmployeesDTO {
    @NotNull(message = "Выберите питомца")
    private Long animalId;

    private List<Long> employeeIds;

    public CreateAppointmentChooseEmployeesDTO() {
        this.employeeIds = new ArrayList<>();
    }
}
