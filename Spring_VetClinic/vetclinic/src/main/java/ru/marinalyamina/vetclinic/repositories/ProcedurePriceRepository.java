package ru.marinalyamina.vetclinic.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.marinalyamina.vetclinic.models.entities.ProcedurePrice;

import java.util.Optional;

@Repository
public interface ProcedurePriceRepository extends JpaRepository<ProcedurePrice, Long> {
    Optional<ProcedurePrice> findTopByProcedureIdOrderByStartDateDesc(Long procedureId);
}
