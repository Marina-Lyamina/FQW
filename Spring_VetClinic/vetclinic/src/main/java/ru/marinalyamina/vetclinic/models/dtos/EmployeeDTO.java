package ru.marinalyamina.vetclinic.models.dtos;

import lombok.Getter;
import lombok.Setter;
import ru.marinalyamina.vetclinic.models.entities.Employee;

@Getter
@Setter
public class EmployeeDTO {
    private Long id;
    private String fullName;
    private String position;
    private String imageBase64;

    public EmployeeDTO(Employee employee, String imageBase64) {
        this.id = employee.getId();
        this.fullName = employee.getUser().getFIO();
        this.position = employee.getPosition().getName();
        this.imageBase64 = imageBase64;
    }
}
