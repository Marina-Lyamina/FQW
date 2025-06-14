package ru.marinalyamina.vetclinic.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.marinalyamina.vetclinic.models.entities.Diagnosis;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateAppointmentDTO {

    private Long id;

    private String reason;

    private Diagnosis diagnosis;

    private String medicalPrescription;
}
