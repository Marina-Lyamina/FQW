package ru.marinalyamina.vetclinic.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.marinalyamina.vetclinic.models.entities.Schedule;
import ru.marinalyamina.vetclinic.models.enums.Role;
import ru.marinalyamina.vetclinic.services.ClientService;
import ru.marinalyamina.vetclinic.services.EmployeeService;
import ru.marinalyamina.vetclinic.services.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final UserService userService;
    private final EmployeeService employeeService;
    private final ClientService clientService;

    public HomeController(UserService userService, EmployeeService employeeService, ClientService clientService) {
        this.userService = userService;
        this.employeeService = employeeService;
        this.clientService = clientService;
    }

    /*@GetMapping("/")
    public String home(Model model) {
        var currentUser = userService.getCurrentUser();

        if (currentUser.isPresent()) {
            var employee = employeeService.getAll().stream()
                    .filter(e -> e.getUser().getId().equals(currentUser.get().getId()))
                    .findFirst();

            var client = clientService.getAll().stream()
                    .filter(c -> c.getUser().getId().equals(currentUser.get().getId()))
                    .findFirst();

            if (employee.isPresent()) {
                // Получаем все будущие записи расписания для сотрудника
                List<Schedule> futureSchedules = employee.get().getSchedules().stream()
                        .filter(s -> s != null && s.getDate() != null && s.getDate().isAfter(LocalDateTime.now()))
                        .sorted(Comparator.comparing(Schedule::getDate))
                        .collect(Collectors.toList());

                // Разделяем на занятые и свободные слоты
                List<Schedule> bookedSchedules = futureSchedules.stream()
                        .filter(s -> s.getAnimal() != null)
                        .collect(Collectors.toList());

                List<Schedule> freeSchedules = futureSchedules.stream()
                        .filter(s -> s.getAnimal() == null)
                        .collect(Collectors.toList());

                model.addAttribute("employee", employee.get());
                model.addAttribute("isAdmin", currentUser.get().getRole() == Role.ROLE_ADMIN);
                model.addAttribute("bookedSchedules", bookedSchedules);
                model.addAttribute("freeSchedules", freeSchedules);

                return "index";
            } else if (client.isPresent()) {
                model.addAttribute("client", client.get());
                return "auth_clients/client_index";
            }
        }
        return "public/index";
    }*/

    @GetMapping("/")
    public String home(Model model) {
        var currentUser = userService.getCurrentUser();

        if (currentUser.isPresent()) {
            var employee = employeeService.getAll().stream()
                    .filter(e -> e.getUser().getId().equals(currentUser.get().getId()))
                    .findFirst();

            var client = clientService.getAll().stream()
                    .filter(c -> c.getUser().getId().equals(currentUser.get().getId()))
                    .findFirst();

            if (employee.isPresent()) {

                model.addAttribute("employee", employee.get());
                model.addAttribute("isAdmin", currentUser.get().getRole() == Role.ROLE_ADMIN);

                return "index";
            } else if (client.isPresent()) {
                model.addAttribute("client", client.get());
                return "auth_clients/client_index";
            }
        }
        return "public/index";
    }






}
