package ru.marinalyamina.vetclinic.controllers;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.marinalyamina.vetclinic.models.entities.Position;
import ru.marinalyamina.vetclinic.models.enums.Role;
import ru.marinalyamina.vetclinic.services.EmployeeService;
import ru.marinalyamina.vetclinic.services.PositionService;
import ru.marinalyamina.vetclinic.services.UserService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/positions")
public class PositionController {
    private final PositionService positionService;
    private final UserService userService;
    private final EmployeeService employeeService;

    public PositionController(PositionService positionService, UserService userService,
                              EmployeeService employeeService) {
        this.positionService = positionService;
        this.userService = userService;
        this.employeeService = employeeService;
    }

    /*@GetMapping("/all")
    public String getAll(Model model) {
        model.addAttribute("positions", positionService.getAll());

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
        return "positions/all";
    }*/

    @GetMapping("/all")
    public String getAll(@RequestParam(required = false) String keyword, Model model) {
        List<Position> positions = positionService.findFiltered(keyword);
        model.addAttribute("positions", positions);
        model.addAttribute("keyword", keyword);

        var currentUser = userService.getCurrentUser();

        if (currentUser.isPresent()) {
            var employee = employeeService.getAll().stream()
                    .filter(e -> e.getUser().getId().equals(currentUser.get().getId()))
                    .findFirst();

            if (employee.get().getUser().getRole() == Role.ROLE_ADMIN) {
                model.addAttribute("isAdmin", true);
            } else {
                model.addAttribute("isAdmin", false);
            }
        }

        return "positions/all";
    }

    @GetMapping("/table")
    public String getPositionTableFragment(@RequestParam(required = false) String keyword, Model model) {
        List<Position> positions = positionService.findFiltered(keyword);
        model.addAttribute("positions", positions);
        return "positions/all :: tableContent";
    }

    @GetMapping("/details/{id}")
    public String details(Model model, @PathVariable("id") Long id) {
        Optional<Position> optionalPosition = positionService.getById(id);
        if(optionalPosition.isEmpty()){
            return "redirect:/positions/all";
        }
        model.addAttribute("position", optionalPosition.get());
        return "positions/details";
    }

    @GetMapping("/create")
    public String createGet(Model model) {
        model.addAttribute("position", new Position());
        return "positions/create";
    }

    @PostMapping("/create")
    public String createPost(@ModelAttribute("position") @Valid Position position, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "positions/create";
        }

        if (positionService.existsByName(position.getName())) {
            bindingResult.rejectValue("name", "error.position", "Должность с таким названием уже существует");
            return "positions/create";
        }

        positionService.create(position);

        return "redirect:/positions/all";
    }

    @GetMapping("/update/{id}")
    public String updateGet(Model model, @PathVariable("id") Long id) {
        Optional<Position> optionalPosition = positionService.getById(id);
        if (optionalPosition.isEmpty()) {
            return "redirect:/positions/all";
        }
        model.addAttribute("position", optionalPosition.get());
        return "positions/update";
    }

    @PostMapping("/update")
    public String updatePost(@ModelAttribute("position") @Valid Position position, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "positions/update";
        }

        Optional<Position> existingPositionOpt = positionService.getById(position.getId());
        if (existingPositionOpt.isEmpty()) {
            return "redirect:/positions/all";
        }

        if (!Objects.equals(position.getName(), existingPositionOpt.get().getName()) && positionService.existsByName(position.getName())) {
            bindingResult.rejectValue("name", "error.position", "Должность с таким названием уже существует");
            return "positions/update";
        }

        positionService.create(position);

        return "redirect:/positions/all";
    }

    @GetMapping("/delete/{id}")
    public String deleteGet(@PathVariable("id") Long id, Model model) {
        Optional<Position> optionalPosition = positionService.getById(id);
        if(optionalPosition.isEmpty()){
            return "redirect:/positions/all";
        }

        model.addAttribute("position", optionalPosition.get());
        model.addAttribute("hasEmployees", optionalPosition.get().getEmployees() != null && !optionalPosition.get().getEmployees().isEmpty());

        return "positions/delete";
    }

    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable("id") Long id, Model model) {
        Optional<Position> optionalPosition = positionService.getById(id);
        if(optionalPosition.isEmpty()){
            return "redirect:/positions/all";
        }

        Position position = optionalPosition.get();

        if(position.getEmployees() != null && !position.getEmployees().isEmpty()){
            model.addAttribute("position", position);
            model.addAttribute("hasEmployees", true);
            return "positions/delete";
        }

        if(positionService.existsById(id)){
            positionService.delete(id);
        }
        return "redirect:/positions/all";
    }
}
