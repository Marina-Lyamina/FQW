package ru.marinalyamina.vetclinic.controllers;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.marinalyamina.vetclinic.models.entities.ProcedureCategory;
import ru.marinalyamina.vetclinic.models.enums.Role;
import ru.marinalyamina.vetclinic.services.EmployeeService;
import ru.marinalyamina.vetclinic.services.ProcedureCategoryService;
import ru.marinalyamina.vetclinic.services.UserService;

import java.util.List;

@Controller
@RequestMapping("/procedure-categories")
public class ProcedureCategoryController {
    private final ProcedureCategoryService categoryService;
    private final UserService userService;
    private final EmployeeService employeeService;

    public ProcedureCategoryController(ProcedureCategoryService categoryService, UserService userService, EmployeeService employeeService) {
        this.categoryService = categoryService;
        this.userService = userService;
        this.employeeService = employeeService;
    }

    @GetMapping("/all")
    public String getAllCategories(Model model) {
        List<ProcedureCategory> categories = categoryService.getAll();
        model.addAttribute("categories", categories);

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

        return "procedure-categories/all";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("procedureCategory", new ProcedureCategory());
        return "procedure-categories/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("procedureCategory") @Valid ProcedureCategory category,
                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "procedure-categories/create";
        }
        categoryService.create(category);
        return "redirect:/procedure-categories/all";
    }

    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable Long id, Model model) {
        ProcedureCategory category = categoryService.getById(id).orElseThrow(() -> new IllegalArgumentException("Неверный ID: " + id));
        model.addAttribute("procedureCategory", category);
        return "procedure-categories/update";
    }

    //
    @PostMapping("/update")
    public String update(@ModelAttribute("procedureCategory") @Valid ProcedureCategory category,
                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "procedure-categories/update";
        }
        categoryService.create(category);
        return "redirect:/procedure-categories/all";
    }

    @GetMapping("/delete/{id}")
    public String showDeleteConfirmation(@PathVariable Long id, Model model) {
        ProcedureCategory category = categoryService.getById(id).orElseThrow(() -> new IllegalArgumentException("Неверный ID: " + id));
        boolean hasProcedures = category.getProcedures() != null && !category.getProcedures().isEmpty();
        model.addAttribute("procedureCategory", category);
        model.addAttribute("hasProcedures", hasProcedures);
        return "procedure-categories/delete";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        categoryService.delete(id);
        return "redirect:/procedure-categories/all";
    }
}
