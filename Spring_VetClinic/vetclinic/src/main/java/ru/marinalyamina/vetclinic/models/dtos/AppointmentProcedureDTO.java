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
public class AppointmentProcedureDTO {
    @NotNull(message = "Выберите прием")
    private Long appointmentId;

    @NotNull(message = "Выберите услугу")
    private Long procedureId;
}
