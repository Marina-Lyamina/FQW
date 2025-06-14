package ru.marinalyamina.vetclinic.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.marinalyamina.vetclinic.models.entities.Procedure;

import java.util.List;

@Repository
public interface ProcedureRepository extends JpaRepository<Procedure, Long>, JpaSpecificationExecutor<Procedure> {
    boolean existsByName(String name);

    boolean existsByNameAndCategoryIdAndAnimalTypeId(String name, Long categoryId, Long animalTypeId);

    /*@Query("""
    SELECT p FROM Procedure p
    WHERE (:categoryId IS NULL OR p.category.id = :categoryId)
      AND (:animalTypeId IS NULL OR p.animalType.id = :animalTypeId OR p.animalType IS NULL)
      AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:onlyActive = FALSE OR p.active = TRUE)
    """)
    List<Procedure> findFiltered(
            @Param("categoryId") Long categoryId,
            @Param("animalTypeId") Long animalTypeId,
            @Param("keyword") String keyword,
            @Param("onlyActive") Boolean onlyActive
    );*/

}
