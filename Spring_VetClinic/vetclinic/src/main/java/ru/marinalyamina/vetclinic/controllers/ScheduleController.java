package ru.marinalyamina.vetclinic.controllers;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.marinalyamina.vetclinic.models.dtos.CreateAppointmentChooseEmployeesDTO;
import ru.marinalyamina.vetclinic.models.dtos.CreateScheduleForAnimalChooseEmployeeDTO;
import ru.marinalyamina.vetclinic.models.entities.*;
import ru.marinalyamina.vetclinic.services.AnimalService;
import ru.marinalyamina.vetclinic.services.EmployeeService;
import ru.marinalyamina.vetclinic.services.ScheduleService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/schedules")
public class ScheduleController {

    private final EmployeeService employeeService;
    private final ScheduleService scheduleService;
    private final AnimalService animalService;

    public ScheduleController(EmployeeService employeeService, ScheduleService scheduleService, AnimalService animalService){
        this.employeeService = employeeService;
        this.scheduleService = scheduleService;
        this.animalService = animalService;
    }

    @GetMapping("/create")
    public String createGet(@RequestParam Long employeeId, Model model) {
        Optional<Employee> optionalEmployee = employeeService.getById(employeeId);
        if (optionalEmployee.isEmpty()) {
            return "redirect:/employees/all";
        }
        Employee employee = optionalEmployee.get();

        var schedule = new Schedule();
        schedule.setEmployee(employee);

        model.addAttribute("schedule", schedule);

        return "schedules/create";
    }

    @PostMapping("/create")
    public String createPost(@ModelAttribute("schedule") @Valid Schedule schedule, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "schedules/create";
        }

        if (schedule.getDate().isBefore(LocalDateTime.now())) {
            bindingResult.rejectValue("date", "error.schedule", "Нельзя указать прошедшую дату");
        }

        if (bindingResult.hasErrors()) {
            return "schedules/create";
        }

        scheduleService.create(schedule);

        return "redirect:/employees/details/" + schedule.getEmployee().getId();
    }

    @GetMapping("/delete/{id}")
    public String deleteGet(@PathVariable("id") Long id, Model model) {
        Optional<Schedule> optionalSchedule = scheduleService.getById(id);
        if(optionalSchedule.isEmpty()){
            return "redirect:/employees/all";
        }

        model.addAttribute("schedule", optionalSchedule.get());
        model.addAttribute("hasAnimal", optionalSchedule.get().getAnimal() != null);

        return "schedules/delete";
    }

    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable("id") Long id, Model model) {
        Optional<Schedule> optionalSchedule = scheduleService.getById(id);
        if(optionalSchedule.isEmpty()){
            return "redirect:/employees/all";
        }

        Schedule schedule = optionalSchedule.get();

        if(schedule.getAnimal() != null){
            model.addAttribute("schedule", schedule);
            model.addAttribute("hasAnimal", true);
            return "schedules/delete";
        }

        if(scheduleService.existsById(id)){
            scheduleService.delete(id);
        }

        return "redirect:/employees/details/" + schedule.getEmployee().getId();
    }

    @GetMapping("/createForAnimal")
    public String createForAnimalGet(@RequestParam Long animalId, Model model) {
        Optional<Animal> optionalAnimal = animalService.getById(animalId);
        if (optionalAnimal.isEmpty()) {
            return "redirect:/";
        }

        var chooseEmployee = new CreateScheduleForAnimalChooseEmployeeDTO();
        chooseEmployee.setAnimalId(animalId);

        model.addAttribute("chooseEmployee", chooseEmployee);
        model.addAttribute("employees", employeeService.getAll());

        return "schedules/createForAnimalChooseEmployee";
    }

    @PostMapping("/createForAnimalChooseEmployee")
    public String createForAnimalChooseEmployee(@ModelAttribute("chooseEmployee") @Valid CreateScheduleForAnimalChooseEmployeeDTO chooseEmployee, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("employees", employeeService.getAll());
            return "schedules/createForAnimalChooseEmployee";
        }

        var employee = employeeService.getById(chooseEmployee.getEmployeeId());

        Schedule schedule = new Schedule();

        schedule.setEmployee(employee.get());
        schedule.setAnimal(animalService.getById(chooseEmployee.getAnimalId()).get());

        List<Schedule> freeEmployeeSchedules = employee.get().getSchedules().stream()
                .filter(sch -> sch.getDate().isAfter(LocalDateTime.now()) && sch.getAnimal() == null)
                .sorted(Comparator.comparing(Schedule::getDate))
                .toList();

        model.addAttribute("schedule", schedule);
        model.addAttribute("freeEmployeeSchedules", freeEmployeeSchedules);

        return "schedules/createForAnimal";
    }

    @PostMapping("/createForAnimal")
    public String createForAnimalPost(@ModelAttribute("schedule") Schedule schedule, BindingResult bindingResult, Model model) {
        if(schedule.getId() == null){
            bindingResult.rejectValue("id", "error.schedule", "Выберите запись");
        }

        if (bindingResult.hasErrors()) {
            List<Schedule> freeEmployeeSchedules = schedule.getEmployee().getSchedules().stream()
                    .filter(sch -> sch.getDate().isAfter(LocalDateTime.now()) && sch.getAnimal() == null)
                    .sorted(Comparator.comparing(Schedule::getDate))
                    .toList();
            model.addAttribute("freeEmployeeSchedules", freeEmployeeSchedules);
            return "schedules/createForAnimal";
        }

        var existingScheduleOptional = scheduleService.getById(schedule.getId());
        var existingSchedule = existingScheduleOptional.get();
        existingSchedule.setAnimal(schedule.getAnimal());

        scheduleService.update(existingSchedule);

        return "redirect:/animals/details/" + schedule.getAnimal().getId();
    }

    @GetMapping("/deleteForAnimal/{id}")
    public String deleteForAnimal(Model model, @PathVariable("id") Long id) {
        Optional<Schedule> optionalSchedule = scheduleService.getById(id);
        if(optionalSchedule.isEmpty()){
            return "redirect:/";
        }

        Schedule schedule = optionalSchedule.get();
        var animalId = schedule.getAnimal().getId();
        schedule.setAnimal(null);
        scheduleService.update(schedule);

        return "redirect:/animals/details/" + animalId;
    }

    @GetMapping("/all")
    public String getAll(Model model) {
        model.addAttribute("employees", employeeService.getAll());
        return "schedules/allEmp";
    }

}
