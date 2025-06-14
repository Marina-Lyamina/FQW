package ru.marinalyamina.vetclinic.models.dtos;

import lombok.Getter;
import lombok.Setter;
import ru.marinalyamina.vetclinic.models.entities.Procedure;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CategoryGroupedProceduresDTO {
    private String categoryName;
    private Map<String, List<Procedure>> animalTypeToProcedures;
}
