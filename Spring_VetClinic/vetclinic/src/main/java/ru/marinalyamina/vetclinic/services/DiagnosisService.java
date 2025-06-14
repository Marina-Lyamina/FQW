package ru.marinalyamina.vetclinic.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.marinalyamina.vetclinic.models.entities.Diagnosis;
import ru.marinalyamina.vetclinic.repositories.DiagnosisRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DiagnosisService {
    private final DiagnosisRepository diagnosisRepository;

    public DiagnosisService(DiagnosisRepository diagnosisRepository) {
        this.diagnosisRepository = diagnosisRepository;
    }

    public List<Diagnosis> getAll() {
        return diagnosisRepository.findAll();
    }

    public Optional<Diagnosis> getById(Long id) {
        return diagnosisRepository.findById(id);
    }

    public boolean existsById(Long id) {
        return diagnosisRepository.existsById(id);
    }

    public boolean existsByName(String name) {
        return diagnosisRepository.existsByName(name);
    }

    public Diagnosis create(Diagnosis diagnosis) {
        return diagnosisRepository.save(diagnosis);
    }

    public void delete(Long id) {
        diagnosisRepository.deleteById(id);
    }

    @Transactional
    public DiagnosisImportResult saveUniqueDiagnoses(List<Diagnosis> diagnoses) {
        List<Diagnosis> saved = new ArrayList<>();
        List<Diagnosis> duplicates = new ArrayList<>();

        for (Diagnosis diagnosis : diagnoses) {
            String name = diagnosis.getName().trim();

            // Проверяем существование по имени (игнорируя регистр)
            if (!diagnosisRepository.existsByNameIgnoreCase(name)) {
                try {
                    // Создаем новый объект с очищенным именем
                    Diagnosis newDiagnosis = new Diagnosis();
                    newDiagnosis.setName(name);
                    // ID НЕ устанавливаем - пусть генерируется автоматически

                    Diagnosis savedDiagnosis = diagnosisRepository.save(newDiagnosis);
                    saved.add(savedDiagnosis);
                } catch (Exception e) {
                    // Если все же произошла ошибка дублирования, добавляем в дубликаты
                    System.err.println("Ошибка при сохранении диагноза '" + name + "': " + e.getMessage());
                    duplicates.add(diagnosis);
                }
            } else {
                duplicates.add(diagnosis);
            }
        }

        return new DiagnosisImportResult(saved, duplicates);
    }

    public static class DiagnosisImportResult {
        private final List<Diagnosis> saved;
        private final List<Diagnosis> duplicates;

        public DiagnosisImportResult(List<Diagnosis> saved, List<Diagnosis> duplicates) {
            this.saved = saved;
            this.duplicates = duplicates;
        }

        public List<Diagnosis> getSaved() {
            return saved;
        }

        public List<Diagnosis> getDuplicates() {
            return duplicates;
        }
    }

    public List<Diagnosis> findFiltered(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return diagnosisRepository.findAll();
        }
        return diagnosisRepository.findByNameContainingIgnoreCase(keyword);
    }
}