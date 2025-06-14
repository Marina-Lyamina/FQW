package ru.marinalyamina.vetclinic.apicontrollers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.marinalyamina.vetclinic.services.EmployeeService;
import ru.marinalyamina.vetclinic.services.ScheduleService;
import ru.marinalyamina.vetclinic.services.UserService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/web_schedules")
public class WebSheduleApiController {

    @Autowired
    final private UserService userService;
    @Autowired
    final private EmployeeService employeeService;
    @Autowired
    final private ScheduleService scheduleService;

    public WebSheduleApiController(ScheduleService scheduleService, UserService userService, EmployeeService employeeService
                                   ){
        this.userService = userService;
        this.employeeService = employeeService;
        this.scheduleService = scheduleService;
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
                    if (schedule.getAnimal() != null) {
                        String clientFio = schedule.getAnimal().getClient().getUser().getFIO();
                        String petName = schedule.getAnimal().getName();
                        event.put("title", clientFio + " — " + petName);
                    } else {
                        event.put("title", ""); // ничего не пишем
                    }
                    event.put("start", schedule.getDate().toString()); // ISO формат
                    return event;
                }).toList();
    }

    @GetMapping("/vet/{id}")
    public List<Map<String, Object>> getEmployeeSchedule(@PathVariable Long id) {
        var employee = employeeService.getAll().stream()
                .filter(e -> e.getId().equals(id))
                .findFirst();

        if (employee.isEmpty()) {
            return List.of();
        }

        return employee.get().getSchedules().stream()
                .filter(schedule -> schedule.getDate().isAfter(LocalDateTime.now()))
                .map(schedule -> {
                    Map<String, Object> event = new HashMap<>();
                    event.put("id", schedule.getId()); // этооооооооооооооооооооооооооооо
                    if (schedule.getAnimal() != null) {
                        String clientFio = schedule.getAnimal().getClient().getUser().getFIO();
                        String petName = schedule.getAnimal().getName();
                        event.put("title", clientFio + " — " + petName);
                    } else {
                        event.put("title", ""); // свободный слот
                    }
                    event.put("start", schedule.getDate().toString());
                    return event;
                }).toList();
    }

    @PostMapping("/vet/{id}/add-free-slots")
    public ResponseEntity<?> addFreeSlots(@PathVariable Long id, @RequestBody List<Map<String, String>> slots) {
        var employee = employeeService.getAll().stream()
                .filter(e -> e.getId().equals(id)).findFirst();
        if (employee.isEmpty()) return ResponseEntity.badRequest().body("Сотрудник не найден");

        for (Map<String, String> slot : slots) {
            try {
                var dateTime = LocalDateTime.parse(slot.get("start"));
                scheduleService.createFreeSlot(employee.get(), dateTime);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Ошибка в дате: " + slot);
            }
        }
        return ResponseEntity.ok().build();
    }

    /*@PostMapping("/vet/{id}/remove-slot")
    public ResponseEntity<?> removeSlot(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        var employee = employeeService.getAll().stream()
                .filter(e -> e.getId().equals(id)).findFirst();
        if (employee.isEmpty()) return ResponseEntity.badRequest().body("Сотрудник не найден");

        try {
            var dateTime = LocalDateTime.parse(payload.get("start"));
            scheduleService.deleteFreeSlotByDate(employee.get(), dateTime);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Некорректный формат даты");
        }
    }*/

    @DeleteMapping("/vet/{employeeId}/remove-slot/{slotId}")
    public ResponseEntity<?> removeSlot(@PathVariable Long employeeId, @PathVariable Long slotId) {
        var employee = employeeService.getAll().stream()
                .filter(e -> e.getId().equals(employeeId))
                .findFirst();

        var schedule = scheduleService.getById(slotId);

        if (employee.isEmpty() || schedule.isEmpty()) {
            return ResponseEntity.badRequest().body("Сотрудник или слот не найден");
        }

        var slot = schedule.get();

        if (!slot.getEmployee().getId().equals(employeeId)) {
            return ResponseEntity.badRequest().body("Слот не принадлежит сотруднику");
        }

        if (slot.getAnimal() != null) {
            return ResponseEntity.badRequest().body("Слот уже занят");
        }

        scheduleService.delete(slotId);
        return ResponseEntity.ok().build();
    }




}



