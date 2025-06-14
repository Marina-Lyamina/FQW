package ru.marinalyamina.vetclinic.services;

import org.springframework.stereotype.Service;
import ru.marinalyamina.vetclinic.models.dtos.CategoryGroupedProceduresDTO;
import ru.marinalyamina.vetclinic.models.entities.Procedure;
import ru.marinalyamina.vetclinic.models.entities.ProcedureCategory;
import ru.marinalyamina.vetclinic.repositories.ProcedureCategoryRepository;

import java.util.*;

@Service
public class ProcedureCategoryService {
    private final ProcedureCategoryRepository procedureCategoryRepository;

    public ProcedureCategoryService(ProcedureCategoryRepository procedureCategoryRepository) {
        this.procedureCategoryRepository = procedureCategoryRepository;
    }

    public List<ProcedureCategory> getAll() { return procedureCategoryRepository.findAll();}

    public Optional<ProcedureCategory> getById(Long id) {
        return procedureCategoryRepository.findById(id);
    }

    public boolean existsById(Long id) {
        return procedureCategoryRepository.existsById(id);
    }

    public boolean existsByName(String name) {
        return procedureCategoryRepository.existsByName(name);
    }

    public ProcedureCategory create(ProcedureCategory procedureCategory) {
        return procedureCategoryRepository.save(procedureCategory);
    }

    public void delete(Long id) {
        procedureCategoryRepository.deleteById(id);
    }

    public List<CategoryGroupedProceduresDTO> getAllGroupedProcedures() {
        List<ProcedureCategory> categories = procedureCategoryRepository.findAllWithProcedures();

        List<CategoryGroupedProceduresDTO> result = new ArrayList<>();

        for (ProcedureCategory category : categories) {
            CategoryGroupedProceduresDTO dto = new CategoryGroupedProceduresDTO();
            dto.setCategoryName(category.getName());

            Map<String, List<Procedure>> map = new LinkedHashMap<>();

            for (Procedure proc : category.getProcedures()) {
                String key = (proc.getAnimalType() == null) ? "Для всех животных" : proc.getAnimalType().getName();
                map.computeIfAbsent(key, k -> new ArrayList<>()).add(proc);
            }

            dto.setAnimalTypeToProcedures(map);
            result.add(dto);
        }

        return result;
    }

}
