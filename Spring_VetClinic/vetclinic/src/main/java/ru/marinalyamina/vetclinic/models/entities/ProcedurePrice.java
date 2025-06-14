package ru.marinalyamina.vetclinic.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Optional;

@Entity
@Table(name = "procedure_prices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcedurePrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "procedure_id")
    private Procedure procedure;

    @Column(nullable = false)
    @NotNull(message = "Введите цену")
    private Integer price;

    @Column(name = "start_date", nullable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, fallbackPatterns = {"dd.MM.yyyy HH:mm:ss"})
    private LocalDateTime startDate;



}

