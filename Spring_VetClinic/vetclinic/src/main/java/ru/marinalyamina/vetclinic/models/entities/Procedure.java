package ru.marinalyamina.vetclinic.models.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "procedures")
public class Procedure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 128, unique = true, nullable = false)
    @NotEmpty(message = "Введите название")
    @Size(max = 128, message = "Название не должно превышать 128 символов")
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    /*@Column(nullable = false)
    @NotNull(message = "Введите стоимость")
    private Integer price;*/

    @OneToMany(mappedBy = "procedure", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProcedurePrice> prices;


    @ManyToMany(mappedBy = "procedures")
    @JsonBackReference
    private List<Appointment> appointments;

    //----
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private ProcedureCategory category;

    @ManyToOne
    @JoinColumn(name = "animal_type_id") // nullable = true по умолчанию
    private AnimalType animalType;

    public Optional<ProcedurePrice> getCurrentPrice() {
        return prices == null ? Optional.empty() :
                prices.stream()
                        .filter(p -> p.getStartDate().isBefore(LocalDateTime.now()))
                        .max((a, b) -> a.getStartDate().compareTo(b.getStartDate()));
    }

    public Integer getPrice() {
        return getCurrentPrice().map(ProcedurePrice::getPrice).orElse(null);
    }
}
