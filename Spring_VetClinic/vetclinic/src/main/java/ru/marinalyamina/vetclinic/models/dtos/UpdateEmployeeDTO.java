package ru.marinalyamina.vetclinic.models.dtos;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.marinalyamina.vetclinic.models.entities.Position;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateEmployeeDTO {
    private Long id;

    private String description;

    @Valid
    private UpdateUserDTO user;

    private Position position;
}
