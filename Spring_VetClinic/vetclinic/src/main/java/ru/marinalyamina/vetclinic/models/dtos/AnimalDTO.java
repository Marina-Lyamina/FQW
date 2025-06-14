package ru.marinalyamina.vetclinic.models.dtos;

import lombok.Getter;
import lombok.Setter;
import ru.marinalyamina.vetclinic.models.entities.Animal;
import ru.marinalyamina.vetclinic.models.entities.Appointment;
import ru.marinalyamina.vetclinic.models.entities.Schedule;

import java.util.List;

@Getter
@Setter
public class AnimalDTO {
    private Long id;
    private String name;
    private String mainImageBase64;
    private List<Schedule> schedules;

    public AnimalDTO(Animal animal, String base64Image) {
        this.id = animal.getId();
        this.name = animal.getName();
        this.mainImageBase64 = base64Image;
    }

}
