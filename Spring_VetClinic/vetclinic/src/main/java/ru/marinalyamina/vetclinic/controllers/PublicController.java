package ru.marinalyamina.vetclinic.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Valid;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.marinalyamina.vetclinic.models.dtos.*;
import ru.marinalyamina.vetclinic.models.entities.Employee;
import ru.marinalyamina.vetclinic.services.*;
import ru.marinalyamina.vetclinic.utils.FileManager;

import java.io.IOException;
import java.util.*;

@Controller
public class PublicController {

    private final UserService userService;
    private final EmployeeService employeeService;
    private final ClientService clientService;
    private final ScheduleService scheduleService;
    private final AnimalService animalService;
    private final ProcedureCategoryService procedureCategoryService;

    public PublicController(UserService userService, EmployeeService employeeService, ClientService clientService,
                            ScheduleService scheduleService, AnimalService animalService, ProcedureCategoryService procedureCategoryService) {
        this.userService = userService;
        this.employeeService = employeeService;
        this.clientService = clientService;
        this.animalService = animalService;
        this.scheduleService = scheduleService;
        this.procedureCategoryService = procedureCategoryService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/vets")
    public String vets(Model model) {

        var currentUser = userService.getCurrentUser();

        List<Employee> employees = employeeService.getAll();

        List<EmployeeWithOnlineDTO> employeeDTOs = employees.stream().map(emp -> {
            try {
                String base64 = (emp.getMainImage() != null)
                        ? FileManager.getFile(emp.getMainImage().getName())
                        : FileManager.getBaseFile("defaultEmployee.jpg");

                return new EmployeeWithOnlineDTO(emp, base64);

            } catch (Exception e) {
                try {
                    return new EmployeeWithOnlineDTO(emp, FileManager.getBaseFile("defaultEmployee.jpg"));
                } catch (IOException ex) {
                    throw new RuntimeException("Ошибка загрузки фото сотрудника", ex);
                }
            }
        }).toList();

        model.addAttribute("employees", employeeDTOs);

        if(currentUser.isEmpty()){
            return "public/vets";
        }
        else{
            return "auth_clients/vets";
        }
    }

    /*@GetMapping("/vets/{id}")
    public String detailsVet(@PathVariable("id") Long id, Model model) throws JsonProcessingException {

        var currentUser = userService.getCurrentUser();

        Optional<Employee> optionalEmployee = employeeService.getById(id);

        if(optionalEmployee.isEmpty()){
            return "redirect:/";
        }

        model.addAttribute(optionalEmployee.get());

        try{
            if(optionalEmployee.get().getMainImage() == null){
                model.addAttribute("filePhoto", FileManager.getBaseFile("defaultEmployee.jpg"));
            }
            else{
                model.addAttribute("filePhoto", FileManager.getFile(optionalEmployee.get().getMainImage().getName()));
            }
        }

        catch(Exception e){
            System.err.println("Ошибка при загрузке фото врача: " + e.getMessage());
            return "redirect:/vets";
        }

        model.addAttribute("FIO", optionalEmployee.get().getUser().getFIO());

        if(currentUser.isEmpty()){
            return "public/details_vet";
        }
        else{
            return "auth_clients/details_vet";
        }
    }*/

    @GetMapping("/vets/{id}")
    public String detailsVet(@PathVariable("id") Long id, Model model) throws JsonProcessingException {
        var currentUser = userService.getCurrentUser();
        Optional<Employee> optionalEmployee = employeeService.getById(id);

        if (optionalEmployee.isEmpty()) {
            return "redirect:/";
        }

        Employee employee = optionalEmployee.get();
        model.addAttribute(employee);

        try {
            if (employee.getMainImage() == null) {
                model.addAttribute("filePhoto", FileManager.getBaseFile("defaultEmployee.jpg"));
            } else {
                model.addAttribute("filePhoto", FileManager.getFile(employee.getMainImage().getName()));
            }
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке фото врача: " + e.getMessage());
            return "redirect:/vets";
        }

        model.addAttribute("FIO", employee.getUser().getFIO());

        if (currentUser.isPresent()) {
            // Получение свободных слотов
            List<ScheduleDTO> schedules = scheduleService.getEmployeeFreeSchedules(id);
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            String schedulesJson = mapper.writeValueAsString(schedules);
            model.addAttribute("schedulesJson", schedulesJson);

            // Передача питомцев текущего пользователя
            var pets = animalService.getAnimalsForClient(currentUser.get().getId());
            model.addAttribute("pets", pets);

            return "auth_clients/details_vet";
        } else {
            return "public/details_vet";
        }
    }


    @PostMapping("/pets/schedule")
    public String addAnimalToSchedule(@ModelAttribute @Valid CreateAnimalScheduleDTO dto,
                                      BindingResult bindingResult,
                                      Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "auth_clients/details_vet";
        }

        try {
            scheduleService.createAnimalSchedule(dto);
            return "redirect:/pets"; // куда перенаправлять после успеха
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка: " + e.getMessage());
            return "auth_clients/details_vet";
        }
    }

    @GetMapping("/procedures_prices")
    public String procedures(Model model) {
        var currentUser = userService.getCurrentUser();

        List<CategoryGroupedProceduresDTO> categories = procedureCategoryService.getAllGroupedProcedures();
        model.addAttribute("categories", categories);

        return currentUser.isPresent()
                ? "auth_clients/procedures"
                : "public/procedures";
    }


}
