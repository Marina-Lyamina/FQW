package ru.marinalyamina.vetclinic.apicontrollers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.marinalyamina.vetclinic.models.dtos.CreateAnimalScheduleDTO;
import ru.marinalyamina.vetclinic.models.entities.Schedule;
import ru.marinalyamina.vetclinic.services.EmployeeService;
import ru.marinalyamina.vetclinic.services.ScheduleService;
import ru.marinalyamina.vetclinic.services.UserService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleApiController {

    final private ScheduleService scheduleService;
    @Autowired
    final private UserService userService;
    @Autowired
    final private EmployeeService employeeService;

    public ScheduleApiController(ScheduleService scheduleService, UserService userService, EmployeeService employeeService){
        this.scheduleService = scheduleService;
        this.userService = userService;
        this.employeeService = employeeService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<Schedule> getScheduleById(@PathVariable Long id) {
        Optional<Schedule> scheduleOptional = scheduleService.getById(id);

        if (scheduleOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(scheduleOptional.get());
    }

    @PostMapping("/animals")
    public ResponseEntity<Long> createAnimalSchedule(@Valid @RequestBody CreateAnimalScheduleDTO animalScheduleDTO) {
        Optional<Schedule> scheduleOptional = scheduleService.getById(animalScheduleDTO.getScheduleId());

        if (scheduleOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        scheduleService.createAnimalSchedule(animalScheduleDTO);

        return ResponseEntity.ok(scheduleOptional.get().getId());
    }


    @GetMapping("/vet")
    public List<Map<String, Object>> getSchedules() {

        var currentUser = userService.getCurrentUser();

        if (currentUser.isEmpty()) {
            return List.of();
        }

        var employee = employeeService.getAll().stream()
                .filter(e -> e.getUser().getId().equals(currentUser.get().getId()))
                .findFirst();

        if (employee.isEmpty()) {
            return List.of();
        }

        return employee.get().getSchedules().stream()
                .filter(schedule -> schedule.getDate().isAfter(LocalDateTime.now()))
                .map(schedule -> {
                    Map<String, Object> event = new HashMap<>();
                    String clientFio = schedule.getAnimal() != null ? schedule.getAnimal().getClient().getUser().getFIO() : "Неизвестный клиент";
                    String petName = schedule.getAnimal() != null ? schedule.getAnimal().getName() : "Без питомца";
                    event.put("title", clientFio + " — " + petName);
                    event.put("start", schedule.getDate().toString()); // ISO 8601
                    return event;
                }).toList();
    }
}


