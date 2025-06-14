package ru.marinalyamina.vetclinic.models.dtos;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateFileDTO {

    @NotEmpty(message = "Введите Контент")
    private String fileContent;

    @NotEmpty(message = "Введите Расширение")
    private String fileExtensions;
}
