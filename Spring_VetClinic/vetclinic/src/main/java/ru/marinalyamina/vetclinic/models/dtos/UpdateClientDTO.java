package ru.marinalyamina.vetclinic.models.dtos;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateClientDTO {

    private Long id;

    @Valid
    private UpdateUserDTO user;
}
