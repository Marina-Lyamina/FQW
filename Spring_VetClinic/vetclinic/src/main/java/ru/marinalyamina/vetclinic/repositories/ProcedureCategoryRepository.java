package ru.marinalyamina.vetclinic.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.marinalyamina.vetclinic.models.entities.ProcedureCategory;

import java.util.List;

@Repository
public interface ProcedureCategoryRepository extends JpaRepository<ProcedureCategory, Long> {
    boolean existsByName(String name);

    @Query("SELECT DISTINCT pc FROM ProcedureCategory pc " +
            "LEFT JOIN FETCH pc.procedures p " +
            "LEFT JOIN FETCH p.animalType " +
            "ORDER BY pc.name ASC, p.name ASC")
    List<ProcedureCategory> findAllWithProcedures();
}
