package ru.marinalyamina.vetclinic.controllers;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.marinalyamina.vetclinic.models.dtos.*;
import ru.marinalyamina.vetclinic.models.entities.*;
import ru.marinalyamina.vetclinic.models.enums.Role;
import ru.marinalyamina.vetclinic.services.*;
import ru.marinalyamina.vetclinic.utils.FileManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final ProcedureService procedureService;
    private final AnimalService animalService;
    private final EmployeeService employeeService;
    private final FileService fileService;
    private final UserService userService;
    private final ClientService clientService;
    private final DiagnosisService diagnosisService;
    private final ScheduleService scheduleService;

    public AppointmentController(AppointmentService appointmentService, ProcedureService procedureService,
                                 AnimalService animalService, EmployeeService employeeService, FileService fileService,
                                 UserService userService, ClientService clientService, DiagnosisService diagnosisService,
                                 ScheduleService scheduleService){
        this.appointmentService = appointmentService;
        this.procedureService = procedureService;
        this.animalService = animalService;
        this.employeeService = employeeService;
        this.fileService = fileService;
        this.userService = userService;
        this.clientService = clientService;
        this.diagnosisService = diagnosisService;
        this.scheduleService = scheduleService;
    }

    /*@GetMapping("/all")
    public String getAppointmentPage(@RequestParam(required = false)
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                     LocalDate date,
                                     Model model) {
        var currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) return "redirect:/login";

        var employee = employeeService.getAll().stream()
                .filter(e -> e.getUser().getId().equals(currentUser.get().getId()))
                .findFirst().orElse(null);
        if (employee == null) return "redirect:/";

        if (date == null) date = LocalDate.now();

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<Schedule> todaysSchedules = scheduleService.findByEmployeeAndDateBetweenAndAnimalNotNull(employee, start, end);

        List<Appointment> ongoing = appointmentService.findByEmployeeAndEndDateIsNull(employee);

        List<Appointment> completed = appointmentService.findByEmployeeAndEndDateBetween(employee, start, end);

        model.addAttribute("date", date);
        model.addAttribute("schedules", todaysSchedules);
        model.addAttribute("ongoingAppointments", ongoing);
        model.addAttribute("completedAppointments", completed);

        return "appointments/all";
    }*/

    @GetMapping("/all")
    public String getAppointmentPage(
            @ModelAttribute AppointmentFilterDTO filter,
            Model model) {

        var currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) return "redirect:/login";

        var employee = employeeService.getAll().stream()
                .filter(e -> e.getUser().getId().equals(currentUser.get().getId()))
                .findFirst().orElse(null);

        if (employee == null) return "redirect:/";

        if (filter.getStartDate() == null) filter.setStartDate(LocalDate.now());
        if (filter.getEndDate() == null) filter.setEndDate(filter.getStartDate());

        LocalDateTime startDateTime = filter.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = filter.getEndDate().plusDays(1).atStartOfDay();

        List<Schedule> todaysSchedules = scheduleService.findByEmployeeAndDateBetweenAndAnimalNotNull(
                employee, LocalDate.now().atStartOfDay(), LocalDate.now().plusDays(1).atStartOfDay());

        List<Appointment> ongoing = appointmentService.findByEmployeeAndEndDateIsNull(employee);
        List<Appointment> completed = appointmentService.findByEmployeeAndEndDateBetween(employee, startDateTime, endDateTime);

        model.addAttribute("date", LocalDate.now());
        model.addAttribute("filter", filter);
        model.addAttribute("schedules", todaysSchedules);
        model.addAttribute("ongoingAppointments", ongoing);
        model.addAttribute("completedAppointments", completed);

        return "appointments/all";
    }

    @PostMapping("/complete/{id}")
    public String completeAppointment(@PathVariable Long id) {
        appointmentService.completeAppointment(id);
        return "redirect:/appointments/details/" + id;
    }


    @GetMapping("/details/{id}")
    public String getAppointmentDetails(@PathVariable Long id, Model model) {
        Optional<Appointment> optionalAppointment = appointmentService.getById(id);
        if (optionalAppointment.isEmpty()) {
            return "redirect:/";
        }
        var currentUser = userService.getCurrentUser();

        Appointment appointment = optionalAppointment.get();
        model.addAttribute("appointment", appointment);

        try{
            if(appointment.getFiles() != null){
                var filesPhoto = new ArrayList<>();
                for (DbFile file : appointment.getFiles()) {
                    filesPhoto.add(FileManager.getFile(file.getName()));
                }
                model.addAttribute("filesPhoto", filesPhoto);
            }
        }
        catch(Exception e){
            if(currentUser.isPresent()){
                var employee = employeeService.getAll().stream()
                        .filter(em -> em.getUser().getId().equals(currentUser.get().getId()))
                        .findFirst();

                var client = clientService.getAll().stream()
                        .filter(c -> c.getUser().getId().equals(currentUser.get().getId()))
                        .findFirst();

                if(employee.isPresent()){
                    return "redirect:/animals/details/" + appointment.getAnimal().getId();
                }
                else{
                    return "redirect:/pets/" + appointment.getAnimal().getId();
                }
            }
        }

        if(currentUser.isPresent()){
            var employee = employeeService.getAll().stream()
                    .filter(em -> em.getUser().getId().equals(currentUser.get().getId()))
                    .findFirst();

            var client = clientService.getAll().stream()
                    .filter(c -> c.getUser().getId().equals(currentUser.get().getId()))
                    .findFirst();

            if(employee.isPresent()) {
                return "appointments/details";
            }
            else{
                return "auth_clients/details_app";

            }
        }
        else{
            return "redirect:/";
        }

    }

    @GetMapping("/deleteProcedure")
    public String deleteProcedure(@RequestParam Long appointmentId, @RequestParam Long procedureId){
        Optional<Appointment> optionalAppointment = appointmentService.getById(appointmentId);
        if (optionalAppointment.isEmpty()) {
            return "redirect:/";
        }

        Appointment appointment = optionalAppointment.get();

        if (!appointment.getProcedures().stream().anyMatch(procedure -> procedure.getId().equals(procedureId))) {
            return "redirect:/appointments/details/" + appointmentId;
        }

        appointment.getProcedures().removeIf(procedure -> procedure.getId().equals(procedureId));

        appointmentService.update(appointment);

        return "redirect:/appointments/details/" + appointmentId;
    }

    @GetMapping("/addProcedure/{id}")
    public String addProcedureGet(@PathVariable Long id, Model model) {
        Optional<Appointment> optionalAppointment = appointmentService.getById(id);
        if (optionalAppointment.isEmpty()) {
            return "redirect:/";
        }

        var appointmentProcedure = new AppointmentProcedureDTO();
        appointmentProcedure.setAppointmentId(id);
        model.addAttribute("appointmentProcedure", appointmentProcedure);
        model.addAttribute("procedures", procedureService.getAll());


        return "appointments/addProcedure";
    }

    @PostMapping("/addProcedure")
    public String addProcedurePost(@ModelAttribute("appointmentProcedure") @Valid AppointmentProcedureDTO appointmentProcedureDTO, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("procedures", procedureService.getAll());
            return "appointments/addProcedure";
        }

        Optional<Appointment> optionalAppointment = appointmentService.getById(appointmentProcedureDTO.getAppointmentId());
        if (optionalAppointment.isEmpty()) {
            return "redirect:/";
        }
        Appointment appointment = optionalAppointment.get();

        Optional<Procedure> optionalProcedure = procedureService.getById(appointmentProcedureDTO.getProcedureId());
        if (optionalProcedure.isEmpty()) {
            return "redirect:/";
        }

        Procedure procedure = optionalProcedure.get();

        if (appointment.getProcedures().stream().anyMatch(pr -> pr.getId().equals(procedure.getId()))) {
            return "redirect:/appointments/details/" + appointment.getId();
        }

        appointment.getProcedures().add(procedure);
        appointmentService.update(appointment);

        return "redirect:/appointments/details/" + appointment.getId();
    }

    /*@GetMapping("/update/{id}")
    public String updateGet(@PathVariable("id") Long id, Model model) {
        Optional<Appointment> optionalAppointment = appointmentService.getById(id);
        if (optionalAppointment.isEmpty()) {
            return "redirect:/";
        }
        model.addAttribute("appointment", optionalAppointment.get());
        return "appointments/update";
    }

    @PostMapping("/update")
    public String updatePost(@ModelAttribute("appointment") @Valid UpdateAppointmentDTO appointmentDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "appointments/update";
        }

        Optional<Appointment> existingAppointmentOpt = appointmentService.getById(appointmentDTO.getId());
        if (existingAppointmentOpt.isEmpty()) {
            return "redirect:/";
        }

        Appointment appointment = existingAppointmentOpt.get();

        appointment.setReason(appointmentDTO.getReason());
        appointment.setDiagnosis(appointmentDTO.getDiagnosis());
        appointment.setMedicalPrescription(appointmentDTO.getMedicalPrescription());

        appointmentService.update(appointment);

        return "redirect:/appointments/details/" + appointment.getId();
    }*/

    @GetMapping("/update/{id}")
    public String updateGet(@PathVariable("id") Long id, Model model) {
        Optional<Appointment> optionalAppointment = appointmentService.getById(id);
        if (optionalAppointment.isEmpty()) {
            return "redirect:/";
        }

        model.addAttribute("appointment", optionalAppointment.get());
        model.addAttribute("diagnoses", diagnosisService.getAll()); // Добавляем список диагнозов

        return "appointments/update";
    }

    @PostMapping("/update")
    public String updatePost(@ModelAttribute("appointment") @Valid UpdateAppointmentDTO appointmentDTO,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            // При ошибках валидации нужно заново добавить список диагнозов
            model.addAttribute("diagnoses", diagnosisService.getAll());
            return "appointments/update";
        }

        Optional<Appointment> existingAppointmentOpt = appointmentService.getById(appointmentDTO.getId());
        if (existingAppointmentOpt.isEmpty()) {
            return "redirect:/";
        }

        Appointment appointment = existingAppointmentOpt.get();

        appointment.setReason(appointmentDTO.getReason());
        appointment.setDiagnosis(appointmentDTO.getDiagnosis());
        appointment.setMedicalPrescription(appointmentDTO.getMedicalPrescription());

        appointmentService.update(appointment);

        return "redirect:/appointments/details/" + appointment.getId();
    }

//    @GetMapping("/create")
//    public String createGet(@RequestParam Long animalId, Model model) {
//        Optional<Animal> optionalAnimal = animalService.getById(animalId);
//        if (optionalAnimal.isEmpty()) {
//            return "redirect:/";
//        }
//
//        var chooseEmployees = new CreateAppointmentChooseEmployeesDTO();
//        chooseEmployees.setAnimalId(animalId);
//
//        model.addAttribute("chooseEmployees", chooseEmployees);
//        model.addAttribute("employees", employeeService.getAll());
//
//        return "appointments/createChooseEmployees";
//    }
//
//    @PostMapping("/createChooseEmployees")
//    public String createChooseEmployees(@ModelAttribute("chooseEmployees") @Valid CreateAppointmentChooseEmployeesDTO chooseEmployees,
//                                        BindingResult bindingResult,
//                                        Model model) {
//        if (bindingResult.hasErrors()) {
//            model.addAttribute("employees", employeeService.getAll());
//            return "appointments/createChooseEmployees";
//        }
//
//        var employees = employeeService.getAll().stream()
//                .filter(employee -> chooseEmployees.getEmployeeIds().contains(employee.getId()))
//                .toList();
//
//        Appointment appointment = new Appointment();
//        appointment.setEmployees(employees);
//        appointment.setAnimal(animalService.getById(chooseEmployees.getAnimalId()).get());
//        appointment.setDate(LocalDateTime.now());
//
//        model.addAttribute("appointment", appointment);
//        model.addAttribute("diagnoses", diagnosisService.getAll()); // Добавить список диагнозов
//
//        return "appointments/create";
//    }
//
//    @PostMapping("/create")
//    public String createPost(@ModelAttribute("appointment") @Valid Appointment appointment,
//                             BindingResult bindingResult,
//                             Model model) {
//        if (bindingResult.hasErrors()) {
//            model.addAttribute("diagnoses", diagnosisService.getAll()); // Повторная подгрузка диагнозов
//            return "appointments/create";
//        }
//
//        var newAppointment = appointmentService.create(appointment);
//
//        return "redirect:/appointments/details/" + newAppointment.getId();
//    }

    @GetMapping("/create")
    public String createGet(@RequestParam Long animalId, Model model) {
        var dto = new CreateAppointmentChooseEmployeesDTO();
        dto.setAnimalId(animalId);

        var currentUser = userService.getCurrentUser().orElseThrow();
        var currentEmp = employeeService.getAll().stream()
                .filter(e -> e.getUser().getId().equals(currentUser.getId()))
                .findFirst().orElseThrow();

        // Фильтруем только активных операторов, исключая текущего пользователя
        var employees = employeeService.getAll().stream()
                .filter(e -> e.getUser().getRole() == Role.ROLE_OPERATOR) // Только операторы
                .filter(e -> e.isActive()) // Только неуволенные
                .filter(e -> !e.getId().equals(currentEmp.getId())) // Исключаем текущего
                .collect(Collectors.toList());

        // Получаем уникальные должности для выпадающего списка
        var availablePositions = employees.stream()
                .map(e -> e.getPosition().getName())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        model.addAttribute("chooseEmployees", dto);
        model.addAttribute("employees", employees);
        model.addAttribute("availablePositions", availablePositions);
        model.addAttribute("currentEmp", currentEmp);
        return "appointments/createChooseEmployees";
    }

    @PostMapping("/createChooseEmployees")
    public String createChooseEmployees(@ModelAttribute("chooseEmployees") @Valid CreateAppointmentChooseEmployeesDTO chooseEmployees,
                                        BindingResult bindingResult,
                                        Model model) {
        var currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) return "redirect:/login";

        Optional<Employee> currentEmployeeOpt = employeeService.getAll().stream()
                .filter(e -> e.getUser().getId().equals(currentUser.get().getId()))
                .findFirst();

        // Убеждаемся, что текущий сотрудник всегда включен
        if (currentEmployeeOpt.isPresent()) {
            Long currentId = currentEmployeeOpt.get().getId();
            if (!chooseEmployees.getEmployeeIds().contains(currentId)) {
                chooseEmployees.getEmployeeIds().add(currentId);
            }
        }

        if (bindingResult.hasErrors()) {
            // При ошибках тоже передаем список должностей
            var employees = employeeService.getAll().stream()
                    .filter(e -> e.getUser().getRole() == Role.ROLE_OPERATOR)
                    .filter(e -> e.isActive()) // Только активные
                    .filter(e -> !e.getId().equals(currentEmployeeOpt.get().getId()))
                    .collect(Collectors.toList());

            var availablePositions = employees.stream()
                    .map(e -> e.getPosition().getName())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            model.addAttribute("employees", employees);
            model.addAttribute("availablePositions", availablePositions);
            model.addAttribute("currentEmp", currentEmployeeOpt.get());
            return "appointments/createChooseEmployees";
        }

        var selectedEmployees = employeeService.getAll().stream()
                .filter(e -> chooseEmployees.getEmployeeIds().contains(e.getId()))
                .toList();

        Appointment appointment = new Appointment();
        appointment.setEmployees(selectedEmployees);
        appointment.setAnimal(animalService.getById(chooseEmployees.getAnimalId()).get());
        appointment.setDate(LocalDateTime.now());

        model.addAttribute("appointment", appointment);
        model.addAttribute("diagnoses", diagnosisService.getAll());

        return "appointments/create";
    }

    @GetMapping("/create/employeesTable")
    public String getEmployeesTable(
            @RequestParam Long animalId,
            @RequestParam(required = false) String fio,
            @RequestParam(required = false) String position,
            Model model) {

        var currentUser = userService.getCurrentUser().orElseThrow();
        var currentEmp = employeeService.getAll().stream()
                .filter(e -> e.getUser().getId().equals(currentUser.getId()))
                .findFirst().orElseThrow();

        // Фильтрация активных операторов
        var filtered = employeeService.getAll().stream()
                .filter(e -> e.getUser().getRole() == Role.ROLE_OPERATOR) // Только операторы
                .filter(e -> e.isActive()) // Только активные
                .filter(e -> !e.getId().equals(currentEmp.getId())) // Исключаем текущего
                .filter(e -> fio == null || fio.trim().isEmpty() ||
                        e.getUser().getFIO().toLowerCase().contains(fio.toLowerCase().trim()))
                .filter(e -> position == null || position.isEmpty() ||
                        e.getPosition().getName().equalsIgnoreCase(position))
                .collect(Collectors.toList());

        model.addAttribute("employees", filtered);
        model.addAttribute("animalId", animalId);
        model.addAttribute("currentEmp", currentEmp);
        return "appointments/createChooseEmployees :: employeeTable";
    }

    @PostMapping("/create")
    public String createPost(@ModelAttribute("appointment") @Valid Appointment appointment,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("diagnoses", diagnosisService.getAll()); // Повторная подгрузка диагнозов
            return "appointments/create";
        }

        var newAppointment = appointmentService.create(appointment);

        return "redirect:/appointments/details/" + newAppointment.getId();
    }

    @GetMapping("/delete/{id}")
    public String deleteGet(Model model, @PathVariable("id") Long id) {
        Optional<Appointment> optionalAppointment = appointmentService.getById(id);
        if(optionalAppointment.isEmpty()){
            return "redirect:/";
        }
        Appointment appointment = optionalAppointment.get();

        model.addAttribute("appointment", appointment);

        return "appointments/delete";
    }

    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable("id") Long id, Model model) {
        Optional<Appointment> optionalAppointment = appointmentService.getById(id);
        if(optionalAppointment.isEmpty()){
            return "redirect:/";
        }

        Appointment appointment = optionalAppointment.get();

        appointmentService.delete(appointment.getId());

        return "redirect:/animals/details/" + appointment.getAnimal().getId();
    }

    @GetMapping("/addPhoto/{id}")
    public String addPhotoGet(@PathVariable("id") Long id, Model model) {
        Optional<Appointment> optionalAppointment = appointmentService.getById(id);
        if (optionalAppointment.isEmpty()) {
            return "redirect:/";
        }
        model.addAttribute("appointmentId", id);

        return "appointments/addPhoto";
    }

    @PostMapping("/addPhoto/{id}")
    public String addPhotoPost(Model model, @PathVariable("id") Long id, @RequestParam("image") MultipartFile file) {
        Optional<Appointment> optionalAppointment = appointmentService.getById(id);
        if (optionalAppointment.isEmpty()) {
            return "redirect:/";
        }

        Appointment appointment = optionalAppointment.get();

        DbFile dbFile = new DbFile();


        String fileName = file.getOriginalFilename();
        String extension = ".jpg";
        int index = fileName.lastIndexOf('.');
        if (index > 0 && index < fileName.length() - 1) {
            extension = fileName.substring(index + 1);
        }

        dbFile.setName(FileManager.createFileName(extension));
        dbFile.setDate(LocalDateTime.now());

        try {
            FileManager.saveFile(dbFile.getName(), file.getBytes());
        } catch (IOException e) {
            return "redirect:/appointments/details/" + id;
        }

        fileService.update(dbFile);

        appointment.getFiles().add(dbFile);
        appointmentService.update(appointment);

        return "redirect:/appointments/details/" + id;
    }

}
