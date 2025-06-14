package ru.marinalyamina.vetclinic.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.marinalyamina.vetclinic.models.enums.AnimalGender;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "animals")
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64, nullable = false)
    @NotEmpty(message = "Введите кличку")
    @Size(max = 64, message = "Слишком длинная кличка")
    private String name;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, fallbackPatterns = {"dd.MM.yyyy"})
    private LocalDate birthday;

    private AnimalGender gender;

    /*@Column(length = 64)
    @Size(max = 64, message = "Слишком длинное название породы")
    private String breed;*/

    @OneToOne
    @JsonManagedReference
    private DbFile mainImage;

    @ManyToOne
    @JsonManagedReference
    private AnimalType animalType;

    @ManyToOne
    @JsonManagedReference
    private Breed breed;

    @ManyToOne
    @JsonBackReference
    private Client client;

    @OneToMany(mappedBy = "animal")
    @JsonManagedReference
    private List<Appointment> appointments;

    @OneToMany(mappedBy = "animal")
    @JsonManagedReference
    private List<Schedule> schedules;

    public void initFiles() throws IOException {
        if (mainImage != null){
            mainImage.initContent();
        }
        
    }
}
