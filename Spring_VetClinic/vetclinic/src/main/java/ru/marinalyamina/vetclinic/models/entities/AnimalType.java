package ru.marinalyamina.vetclinic.models.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "animal_types")
public class AnimalType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 128, nullable = false) //, unique = true
    @NotEmpty(message = "Введите название")
    @Size(max = 128, message = "Название не должно превышать 128 символов")
    private String name;

    @OneToMany(mappedBy = "animalType")
    @JsonBackReference
    private List<Animal> animals;

    @OneToMany(mappedBy = "animalType", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Breed> breeds;

    //--
    @OneToMany(mappedBy = "animalType")
    private List<Procedure> procedures;
}
