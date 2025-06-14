package ru.marinalyamina.vetclinic.controllers;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.marinalyamina.vetclinic.models.entities.Diagnosis;
import ru.marinalyamina.vetclinic.models.enums.Role;
import ru.marinalyamina.vetclinic.services.DiagnosisService;
import ru.marinalyamina.vetclinic.services.EmployeeService;
import ru.marinalyamina.vetclinic.services.UserService;
import ru.marinalyamina.vetclinic.utils.DiagnosisCsvParser;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/diagnoses")
public class DiagnosisController {
    private final DiagnosisService diagnosisService;
    private final UserService userService;
    private final EmployeeService employeeService;

    public DiagnosisController(DiagnosisService diagnosisService, UserService userService, EmployeeService employeeService) {
        this.diagnosisService = diagnosisService;
        this.userService = userService;
        this.employeeService = employeeService;
    }

    /*@GetMapping("/all")
    public String getAll(Model model) {
        model.addAttribute("diagnoses", diagnosisService.getAll());
        return "diagnoses/all";
    }*/

    @GetMapping("/all")
    public String getAll(@RequestParam(required = false) String keyword, Model model) {
        List<Diagnosis> diagnoses = diagnosisService.findFiltered(keyword);
        model.addAttribute("diagnoses", diagnoses);
        model.addAttribute("keyword", keyword);

        var currentUser = userService.getCurrentUser();

        if (currentUser.isPresent()) {
            var employee = employeeService.getAll().stream()
                    .filter(e -> e.getUser().getId().equals(currentUser.get().getId()))
                    .findFirst();

            if(employee.get().getUser().getRole() == Role.ROLE_ADMIN){
                model.addAttribute("isAdmin", true);
            }
            else{
                model.addAttribute("isAdmin", false);
            }
        }

        return "diagnoses/all";
    }

    @GetMapping("/table")
    public String getDiagnosisTableFragment(@RequestParam(required = false) String keyword, Model model) {
        List<Diagnosis> diagnoses = diagnosisService.findFiltered(keyword);
        model.addAttribute("diagnoses", diagnoses);
        return "diagnoses/all :: tableContent";
    }

    @GetMapping("/details/{id}")
    public String details(Model model, @PathVariable("id") Long id) {
        Optional<Diagnosis> optionalDiagnosis = diagnosisService.getById(id);
        if(optionalDiagnosis.isEmpty()){
            return "redirect:/diagnoses/all";
        }
        model.addAttribute("diagnosis", optionalDiagnosis.get());
        return "diagnoses/details";
    }

    @GetMapping("/create")
    public String createGet(Model model) {
        model.addAttribute("diagnosis", new Diagnosis());
        return "diagnoses/create";
    }

    @PostMapping("/create")
    public String createPost(@ModelAttribute("diagnosis") @Valid Diagnosis diagnosis, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "diagnoses/create";
        }

        if (diagnosisService.existsByName(diagnosis.getName())) {
            bindingResult.rejectValue("name", "error.diagnosis", "Диагноз с таким названием уже существует");
            return "diagnoses/create";
        }

        diagnosisService.create(diagnosis);

        return "redirect:/diagnoses/all";
    }

    @GetMapping("/update/{id}")
    public String updateGet(Model model, @PathVariable("id") Long id) {
        Optional<Diagnosis> optionalDiagnosis = diagnosisService.getById(id);
        if (optionalDiagnosis.isEmpty()) {
            return "redirect:/diagnoses/all";
        }
        model.addAttribute("diagnosis", optionalDiagnosis.get());
        return "diagnoses/update";
    }

    @PostMapping("/update")
    public String updatePost(@ModelAttribute("diagnosis") @Valid Diagnosis diagnosis, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "diagnoses/update";
        }

        Optional<Diagnosis> existingDiagnosisOpt = diagnosisService.getById(diagnosis.getId());
        if (existingDiagnosisOpt.isEmpty()) {
            return "redirect:/diagnoses/all";
        }

        if (!Objects.equals(diagnosis.getName(), existingDiagnosisOpt.get().getName())
                && diagnosisService.existsByName(diagnosis.getName())) {
            bindingResult.rejectValue("name", "error.diagnosis", "Диагноз с таким названием уже существует");
            return "diagnoses/update";
        }

        diagnosisService.create(diagnosis); // create/update — в одном методе
        return "redirect:/diagnoses/all";
    }

    @GetMapping("/delete/{id}")
    public String deleteGet(Model model, @PathVariable("id") Long id) {
        Optional<Diagnosis> optionalDiagnosis = diagnosisService.getById(id);
        if(optionalDiagnosis.isEmpty()){
            return "redirect:/diagnoses/all";
        }

        model.addAttribute("diagnosis", optionalDiagnosis.get());
        return "diagnoses/delete";
    }

    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable("id") Long id) {
        if(diagnosisService.existsById(id)){
            diagnosisService.delete(id);
        }
        return "redirect:/diagnoses/all";
    }

    @PostMapping("/import")
    public String importDiagnoses(@RequestParam("file") MultipartFile file, Model model) {
        try {
            // Проверяем, что файл не пустой
            if (file.isEmpty()) {
                model.addAttribute("error", "Файл пустой. Пожалуйста, выберите корректный CSV файл.");
                return "diagnoses/all";
            }

            // Проверяем расширение файла
            String fileName = file.getOriginalFilename();
            if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
                model.addAttribute("error", "Пожалуйста, загрузите файл с расширением .csv");
                return "diagnoses/all";
            }

            List<Diagnosis> diagnosesFromFile = DiagnosisCsvParser.parseCSV(file);

            // Проверяем, что в файле есть данные
            if (diagnosesFromFile.isEmpty()) {
                model.addAttribute("error", "В файле не найдено ни одного валидного диагноза. " +
                        "Убедитесь, что файл содержит колонку 'name', 'название' или 'диагноз'.");
                return "diagnoses/all";
            }

            var importResult = diagnosisService.saveUniqueDiagnoses(diagnosesFromFile);

            model.addAttribute("importedCount", importResult.getSaved().size());
            model.addAttribute("skippedCount", importResult.getDuplicates().size());
            model.addAttribute("duplicates", importResult.getDuplicates());

            return "diagnoses/import_result";

        } catch (IOException e) {
            model.addAttribute("error", "Ошибка при чтении файла: " + e.getMessage());
            return "diagnoses/all";
        } catch (Exception e) {
            // Логируем полную ошибку для отладки
            System.err.println("Ошибка при импорте диагнозов: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("error", "Произошла ошибка при импорте: " + e.getMessage() +
                    ". Проверьте формат файла и попробуйте снова.");
            return "diagnoses/all";
        }
    }
}
