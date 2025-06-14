package ru.marinalyamina.vetclinic.services;

import org.springframework.stereotype.Service;

import ru.marinalyamina.vetclinic.models.dtos.CreateProcedureDTO;
import ru.marinalyamina.vetclinic.models.dtos.UpdateProcedureDTO;
import ru.marinalyamina.vetclinic.models.entities.Procedure;
import ru.marinalyamina.vetclinic.models.entities.ProcedurePrice;
import ru.marinalyamina.vetclinic.repositories.AnimalTypeRepository;
import ru.marinalyamina.vetclinic.repositories.ProcedureCategoryRepository;
import ru.marinalyamina.vetclinic.repositories.ProcedureRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.criteria.Predicate;


@Service
public class ProcedureService {
    private final ProcedureRepository procedureRepository;
    private final ProcedureCategoryRepository procedureCategoryRepository;
    private final AnimalTypeRepository animalTypeRepository;

    public ProcedureService(ProcedureRepository procedureRepository, ProcedureCategoryRepository procedureCategoryRepository,
                            AnimalTypeRepository animalTypeRepository) {
        this.procedureRepository = procedureRepository;
        this.procedureCategoryRepository = procedureCategoryRepository;
        this.animalTypeRepository = animalTypeRepository;
    }

    public List<Procedure> getAll() { return procedureRepository.findAll();}

    public Optional<Procedure> getById(Long id) {
        return procedureRepository.findById(id);
    }

    public boolean existsById(Long id) {
        return procedureRepository.existsById(id);
    }

    public boolean existsByName(String name) { return procedureRepository.existsByName(name);};

    public Procedure create(Procedure procedure) {
        return procedureRepository.save(procedure);
    }

    public void delete(Long id) {
        procedureRepository.deleteById(id);
    }

    /*public List<Procedure> findFiltered(Long categoryId, Long animalTypeId, String keyword) {
        return procedureRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            //if (animalTypeId != null) {
             //   predicates.add(cb.equal(root.get("animalType").get("id"), animalTypeId));
            //}

            if (animalTypeId != null) {
                Predicate byAnimalType = cb.equal(root.get("animalType").get("id"), animalTypeId);
                Predicate isUniversal = cb.isNull(root.get("animalType"));
                predicates.add(cb.or(byAnimalType, isUniversal));
            }


            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }*/

    public List<Procedure> findFiltered(Long categoryId, Long animalTypeId, String keyword, Boolean active) {
        return procedureRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (animalTypeId != null) {
                Predicate byAnimalType = cb.equal(root.get("animalType").get("id"), animalTypeId);
                Predicate isUniversal = cb.isNull(root.get("animalType"));
                predicates.add(cb.or(byAnimalType, isUniversal));
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%"));
            }

            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }




    public void createFromForm(CreateProcedureDTO form) {
        Procedure procedure = new Procedure();
        procedure.setName(form.getName());
        procedure.setCategory(procedureCategoryRepository.findById(form.getCategoryId()).orElseThrow());
        if (form.getAnimalTypeId() != null) {
            procedure.setAnimalType(animalTypeRepository.findById(form.getAnimalTypeId()).orElseThrow());
        }

        ProcedurePrice price = new ProcedurePrice();
        price.setProcedure(procedure);
        price.setPrice(form.getPrice());
        price.setStartDate(LocalDateTime.now());

        procedure.setPrices(List.of(price));
        procedureRepository.save(procedure);
    }

    // Добавить в ProcedureService

    public void updateFromForm(UpdateProcedureDTO form) {
        Procedure procedure = procedureRepository.findById(form.getId())
                .orElseThrow(() -> new RuntimeException("Процедура не найдена"));

        procedure.setName(form.getName());
        procedure.setActive(form.isActive());

        // изменилась ли цена
        Integer currentPrice = procedure.getPrice();
        if (currentPrice == null || !currentPrice.equals(form.getPrice())) {
            // Цена изменилась
            ProcedurePrice newPrice = new ProcedurePrice();
            newPrice.setProcedure(procedure);
            newPrice.setPrice(form.getPrice());
            newPrice.setStartDate(LocalDateTime.now());

            if (procedure.getPrices() == null) {
                procedure.setPrices(new ArrayList<>());
            }
            procedure.getPrices().add(newPrice);
        }

        procedureRepository.save(procedure);
    }

    public boolean existsByNameAndCategoryAndAnimalType(String name, Long categoryId, Long animalTypeId) {
        return procedureRepository.existsByNameAndCategoryIdAndAnimalTypeId(name, categoryId, animalTypeId);
    }


}
