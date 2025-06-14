package ru.marinalyamina.vetclinic.controllers;

import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.marinalyamina.vetclinic.models.dtos.CreateProcedureDTO;
import ru.marinalyamina.vetclinic.models.dtos.UpdateProcedureDTO;
import ru.marinalyamina.vetclinic.models.entities.Procedure;
import ru.marinalyamina.vetclinic.models.enums.Role;
import ru.marinalyamina.vetclinic.services.*;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/procedures")
public class ProcedureController {
    private final ProcedureService procedureService;
    private final ProcedureCategoryService procedureCategoryService;
    private final AnimalTypeService animalTypeService;
    private final UserService userService;
    private final EmployeeService employeeService;

    public ProcedureController(ProcedureService procedureService,ProcedureCategoryService procedureCategoryService,
                               AnimalTypeService animalTypeService, UserService userService, EmployeeService employeeService) {
        this.procedureService = procedureService;
        this.procedureCategoryService = procedureCategoryService;
        this.animalTypeService = animalTypeService;
        this.userService = userService;
        this.employeeService = employeeService;
    }

    /*@GetMapping("/all")
    public String getAll(Model model) {
        model.addAttribute("procedures", procedureService.getAll());
        return "procedures/all";
    }*/

    @GetMapping("/all")
    public String getAllProceduresPage(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long animalTypeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            Model model
    ) {
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

        List<Procedure> filtered = procedureService.findFiltered(categoryId, animalTypeId, keyword, active);
        model.addAttribute("procedures", filtered);
        model.addAttribute("categories", procedureCategoryService.getAll());
        model.addAttribute("animalTypes", animalTypeService.getAll());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedAnimalTypeId", animalTypeId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("active", active);
        return "procedures/all";
    }

    @GetMapping("/table")
    public String getProcedureTableFragment(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long animalTypeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            Model model
    ) {
        List<Procedure> filtered = procedureService.findFiltered(categoryId, animalTypeId, keyword, active);
        model.addAttribute("procedures", filtered);
        return "procedures/all :: tableContent";
    }


    @GetMapping("/details/{id}")
    public String details(Model model, @PathVariable("id") Long id) {
        Optional<Procedure> optionalProcedure = procedureService.getById(id);
        if (optionalProcedure.isEmpty()) {
            return "redirect:/procedures/all";
        }
        model.addAttribute("procedure", optionalProcedure.get());
        return "procedures/details";
    }

    /*@GetMapping("/create")
    public String createGet(Model model) {
        model.addAttribute("procedure", new Procedure());
        return "procedures/create";
    }

    @PostMapping("/create")
    public String createPost(@ModelAttribute("procedure") @Valid Procedure procedure, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "procedures/create";
        }

        if (procedureService.existsByName(procedure.getName())) {
            bindingResult.rejectValue("name", "error.procedure", "Услуга с таким названием уже существует");
            return "procedures/create";
        }

        procedureService.create(procedure);

        return "redirect:/procedures/all";
    }*/

    @GetMapping("/create")
    public String createGet(Model model) {
        model.addAttribute("procedureForm", new CreateProcedureDTO());
        model.addAttribute("categories", procedureCategoryService.getAll());
        model.addAttribute("animalTypes", animalTypeService.getAll());
        return "procedures/create";
    }

    @PostMapping("/create")
    public String createPost(@ModelAttribute("procedureForm") @Valid CreateProcedureDTO form,
                             BindingResult bindingResult,
                             Model model) {

        if (procedureService.existsByNameAndCategoryAndAnimalType(form.getName(), form.getCategoryId(), form.getAnimalTypeId())) {
            bindingResult.rejectValue("name", "error.procedure", "Такая услуга уже существует для данной категории и животного");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", procedureCategoryService.getAll());
            model.addAttribute("animalTypes", animalTypeService.getAll());
            return "procedures/create";
        }

        procedureService.createFromForm(form); // отдельный метод
        return "redirect:/procedures/all";
    }

    /*@GetMapping("/update/{id}")
    public String updateGet(Model model, @PathVariable("id") Long id) {
        Optional<Procedure> optionalProcedure = procedureService.getById(id);
        if (optionalProcedure.isEmpty()) {
            return "redirect:/procedures/all";
        }
        model.addAttribute("procedure", optionalProcedure.get());
        return "procedures/update";
    }

    @PostMapping("/update")
    public String updatePost(@ModelAttribute("procedure") @Valid Procedure procedure, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "procedures/update";
        }

        Optional<Procedure> existingProcedureOpt = procedureService.getById(procedure.getId());
        if (existingProcedureOpt.isEmpty()) {
            return "redirect:/procedures/all";
        }

        if (!Objects.equals(procedure.getName(), existingProcedureOpt.get().getName()) && procedureService.existsByName(procedure.getName())) {
            bindingResult.rejectValue("name", "error.procedure", "Услуга с таким названием уже существует");
            return "procedures/update";
        }

        procedureService.create(procedure);

        return "redirect:/procedures/all";
    }*/

    @GetMapping("/update/{id}")
    public String updateGet(Model model, @PathVariable("id") Long id) {
        Optional<Procedure> optionalProcedure = procedureService.getById(id);
        if (optionalProcedure.isEmpty()) {
            return "redirect:/procedures/all";
        }

        Procedure procedure = optionalProcedure.get();

        UpdateProcedureDTO updateForm = new UpdateProcedureDTO();
        updateForm.setId(procedure.getId());
        updateForm.setName(procedure.getName());
        updateForm.setPrice(procedure.getPrice());
        updateForm.setActive(procedure.isActive());

        model.addAttribute("procedureForm", updateForm);
        model.addAttribute("procedure", procedure);
        return "procedures/update";
    }

    @PostMapping("/update")
    public String updatePost(@ModelAttribute("procedureForm") @Valid UpdateProcedureDTO form,
                             BindingResult bindingResult,
                             Model model) {

        // Проверяем существование процедуры
        Optional<Procedure> existingProcedureOpt = procedureService.getById(form.getId());
        if (existingProcedureOpt.isEmpty()) {
            return "redirect:/procedures/all";
        }

        Procedure existingProcedure = existingProcedureOpt.get();

        if (!existingProcedure.getName().equals(form.getName()) &&
                procedureService.existsByNameAndCategoryAndAnimalType(
                        form.getName(),
                        existingProcedure.getCategory().getId(),
                        existingProcedure.getAnimalType() != null ? existingProcedure.getAnimalType().getId() : null)) {
            bindingResult.rejectValue("name", "error.procedure",
                    "Такая услуга уже существует для данной категории и животного");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("procedure", existingProcedure);
            return "procedures/update";
        }

        procedureService.updateFromForm(form);
        return "redirect:/procedures/all";
    }

    @GetMapping("/delete/{id}")
    public String deleteGet(Model model, @PathVariable("id") Long id) {
        Optional<Procedure> optionalProcedure = procedureService.getById(id);
        if (optionalProcedure.isEmpty()) {
            return "redirect:/procedures/all";
        }

        Procedure procedure = optionalProcedure.get();
        boolean hasAppointments = procedure.getAppointments() != null && !procedure.getAppointments().isEmpty();

        model.addAttribute("procedure", procedure);
        model.addAttribute("hasAppointments", hasAppointments);

        return "procedures/delete";
    }

    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable("id") Long id, Model model) {
        Optional<Procedure> optionalProcedure = procedureService.getById(id);
        if (optionalProcedure.isEmpty()) {
            return "redirect:/procedures/all";
        }

        Procedure procedure = optionalProcedure.get();

        if (procedure.getAppointments() != null && !procedure.getAppointments().isEmpty()) {
            model.addAttribute("procedure", procedure);
            model.addAttribute("hasAppointments", true);
            return "procedures/delete";
        }

        if (procedureService.existsById(id)) {
            procedureService.delete(id);
        }

        return "redirect:/procedures/all";
    }

    @GetMapping("/report")
    public ResponseEntity<byte[]> generateReport() {
        List<Procedure> procedures = procedureService.getAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Услуги");

            // Создание заголовка таблицы
            Row headerRow = sheet.createRow(0);
            String[] headers = {"#", "Название", "Цена"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(createHeaderCellStyle(workbook));
            }

            // Заполнение данных
            int rowIndex = 1;
            for (Procedure procedure : procedures) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(rowIndex - 1); // Номер
                row.createCell(1).setCellValue(procedure.getName()); // Название
                row.createCell(2).setCellValue(procedure.getPrice()); // Цена
            }

            // Автоматическое подгонка ширины столбцов
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Запись в поток
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            // Возврат файла как ответа
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=procedures_report.xlsx")
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // Метод для создания стиля заголовков
    private CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

}
