package ru.marinalyamina.vetclinic.models.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProcedureDTO {
    @NotBlank(message = "Введите название")
    private String name;

    @NotNull(message = "Выберите категорию услуг")
    private Long categoryId;

    private Long animalTypeId; // может быть null

    @NotNull(message = "Введите цену")
    @Min(value = 1, message = "Цена должна быть больше 0")
    private Integer price;

    private boolean active = true;

}

