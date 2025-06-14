package ru.marinalyamina.vetclinic.models.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
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
@Table(name = "breeds")
public class Breed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 128, unique = true, nullable = false)
    @NotEmpty(message = "Введите название")
    @Size(max = 128, message = "Название не должно превышать 128 символов")
    private String name;

    @ManyToOne
    @JoinColumn(name = "animal_type_id", nullable = false)
    @JsonBackReference
    private AnimalType animalType;

    @OneToMany(mappedBy = "breed")
    @JsonBackReference
    private List<Animal> animals;
}
