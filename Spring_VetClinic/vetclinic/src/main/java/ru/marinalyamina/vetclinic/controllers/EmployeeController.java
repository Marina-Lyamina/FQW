package ru.marinalyamina.vetclinic.controllers;

import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.marinalyamina.vetclinic.models.dtos.UpdateEmployeeDTO;
import ru.marinalyamina.vetclinic.models.dtos.UpdateUserDTO;
import ru.marinalyamina.vetclinic.models.entities.*;
import ru.marinalyamina.vetclinic.models.enums.Role;
import ru.marinalyamina.vetclinic.services.EmployeeService;
import ru.marinalyamina.vetclinic.services.FileService;
import ru.marinalyamina.vetclinic.services.PositionService;
import ru.marinalyamina.vetclinic.services.UserService;
import ru.marinalyamina.vetclinic.utils.FileManager;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/employees")
public class EmployeeController {
    private final EmployeeService employeeService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final PositionService positionService;
    private final FileService fileService;

    public EmployeeController(EmployeeService employeeService, UserService userService, PasswordEncoder passwordEncoder, PositionService positionService, FileService fileService){
        this.employeeService = employeeService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.positionService = positionService;
        this.fileService = fileService;
    }

    /*@GetMapping("/all")
    public String getAll(Model model) {
        model.addAttribute("employees", employeeService.getAll());
        return "employees/all";
    }*/

    @GetMapping("/all")
    public String getAllEmployees(@RequestParam(value = "fio", required = false) String fio,
                                  @RequestParam(value = "position", required = false) String position,
                                  @RequestParam(value = "status", required = false) String status,
                                  Model model) {
        List<Employee> employees = employeeService.findByFilters(fio, position, status);
        model.addAttribute("employees", employees);
        model.addAttribute("fio", fio);
        model.addAttribute("position", position);
        model.addAttribute("status", status);
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

        return "employees/all";
    }

    @GetMapping("/table")
    public String getEmployeeTableFragment(@RequestParam(value = "fio", required = false) String fio,
                                           @RequestParam(value = "position", required = false) String position,
                                           @RequestParam(value = "status", required = false) String status,
                                           Model model) {
        List<Employee> employees = employeeService.findByFilters(fio, position, status);
        model.addAttribute("employees", employees);
        return "employees/all :: tableContent";
    }

    @GetMapping("/deactivate/{id}")
    public String deactivate(@PathVariable Long id) {
        employeeService.deactivate(id);
        return "redirect:/employees/all";
    }

    @GetMapping("/toggleActive/{id}")
    public String toggleActive(@PathVariable Long id) {
        employeeService.toggleActive(id);
        return "redirect:/employees/details/" + id;
    }

    @GetMapping("/toggleOnline/{id}")
    public String toggleOnline(@PathVariable Long id) {
        employeeService.toggleOnline(id);
        return "redirect:/employees/details/" + id;
    }


    @GetMapping("/details/{id}")
    public String details(Model model, @PathVariable("id") Long id) {
        Optional<Employee> optionalEmployee = employeeService.getById(id);
        if (optionalEmployee.isEmpty()) {
            return "redirect:/employees/all";
        }

        Employee employee = optionalEmployee.get();

        List<Schedule> futureSchedules = employee.getSchedules().stream()
                .filter(schedule -> schedule.getDate().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(Schedule::getDate))
                .toList();

        employee.setSchedules(futureSchedules);

        model.addAttribute("employee", employee);
        model.addAttribute("isAdminEmp", employee.getUser().getRole() == Role.ROLE_ADMIN);

        try{
            if(employee.getMainImage() == null){
                model.addAttribute("filePhoto", FileManager.getBaseFile("defaultEmployee.jpg"));
            }
            else{
                model.addAttribute("filePhoto", FileManager.getFile(employee.getMainImage().getName()));
            }
        }
        catch(Exception e){
            return "redirect:/employees/all";
        }

        var currentUser = userService.getCurrentUser();

        if (currentUser.isPresent()) {
            var employeeOptional = employeeService.getAll().stream()
                    .filter(e -> e.getUser().getId().equals(currentUser.get().getId()))
                    .findFirst();

            if(employeeOptional.get().getUser().getRole() == Role.ROLE_ADMIN){
                model.addAttribute("isAdmin", true);
            }
            else{
                model.addAttribute("isAdmin", false);
            }
        }

        return "employees/details";
    }

    @GetMapping("/create")
    public String createGet(Model model) {
        List<Position> positions = positionService.getAll();
        model.addAttribute("positions", positions);
        model.addAttribute("employee", new Employee());
        return "employees/create";
    }

    @PostMapping("/create")
    public String createPost(@ModelAttribute("employee") @Valid Employee employee, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            List<Position> positions = positionService.getAll();
            model.addAttribute("positions", positions);
            return "employees/create";
        }

        User user = employee.getUser();
        if (userService.existsByPhone(user.getPhone())) {
            bindingResult.rejectValue("user.phone", "error.employee", "Такой телефон уже использован");
        }
        if (userService.existsByEmail(user.getEmail())) {
            bindingResult.rejectValue("user.email", "error.employee", "Такой email уже использован");
        }
        if (userService.existsByUsername(user.getUsername())) {
            bindingResult.rejectValue("user.username", "error.employee", "Придумайте другой логин");
        }

        if (bindingResult.hasErrors()) {
            List<Position> positions = positionService.getAll();
            model.addAttribute("positions", positions);
            return "employees/create";
        }

        var encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);
        user.setRole(Role.ROLE_OPERATOR);
        userService.create(user);
        employeeService.create(employee);

        return "redirect:/employees/all";
    }

    @GetMapping("/update/{id}")
    public String updateGet(Model model, @PathVariable("id") Long id) {
        Optional<Employee> optionalEmployee = employeeService.getById(id);
        if (optionalEmployee.isEmpty()) {
            return "redirect:/employees/all";
        }

        List<Position> positions = positionService.getAll();
        model.addAttribute("positions", positions);
        model.addAttribute("employee", optionalEmployee.get());
        return "employees/update";
    }


    @PostMapping("/update")
    public String updatePost(@ModelAttribute("employee") @Valid UpdateEmployeeDTO employeeDTO, BindingResult bindingResult, Model model)
    {
        if (bindingResult.hasErrors()) {
            List<Position> positions = positionService.getAll();
            model.addAttribute("positions", positions);
            return "employees/update";
        }
        Optional<Employee> existingEmployeeOpt = employeeService.getById(employeeDTO.getId());
        if (existingEmployeeOpt.isEmpty()) {
            return "redirect:/employees/all";
        }

        Employee existingEmployee = existingEmployeeOpt.get();
        User user = existingEmployee.getUser();

        if (!Objects.equals(user.getPhone(), employeeDTO.getUser().getPhone()) && userService.existsByPhone(employeeDTO.getUser().getPhone())) {
            bindingResult.rejectValue("user.phone", "error.client", "Такой телефон уже использован");
        }
        if (!Objects.equals(user.getEmail(), employeeDTO.getUser().getEmail()) && userService.existsByEmail(employeeDTO.getUser().getEmail())) {
            bindingResult.rejectValue("user.email", "error.client", "Такой email уже использован");
        }
        if (!Objects.equals(user.getUsername(), employeeDTO.getUser().getUsername()) && userService.existsByUsername(employeeDTO.getUser().getUsername())) {
            bindingResult.rejectValue("user.username", "error.client", "Придумайте другой логин");
        }

        if (bindingResult.hasErrors()) {
            List<Position> positions = positionService.getAll();
            model.addAttribute("positions", positions);
            return "employees/update";
        }

        UpdateUserDTO updatedUserDTO = employeeDTO.getUser();

        user.setSurname(updatedUserDTO.getSurname());
        user.setName(updatedUserDTO.getName());
        user.setPatronymic(updatedUserDTO.getPatronymic());
        user.setBirthday(updatedUserDTO.getBirthday());
        user.setEmail(updatedUserDTO.getEmail());
        user.setPhone(updatedUserDTO.getPhone());
        user.setUsername(updatedUserDTO.getUsername());

        existingEmployee.setDescription(employeeDTO.getDescription());
        existingEmployee.setPosition(employeeDTO.getPosition());

        userService.update(user);
        employeeService.update(existingEmployee);

        return "redirect:/employees/all";
    }

    @GetMapping("/delete/{id}")
    public String deleteGet(Model model, @PathVariable("id") Long id) {
        Optional<Employee> optionalEmployee = employeeService.getById(id);
        if (optionalEmployee.isEmpty()) {
            return "redirect:/employees/all";
        }
        Employee employee = optionalEmployee.get();

        boolean hasAppointments = employee.getAppointments() != null && !employee.getAppointments().isEmpty();
        boolean hasSchedules = employee.getSchedules() != null && !employee.getSchedules().isEmpty();

        model.addAttribute("employee", employee);
        model.addAttribute("hasAppointments", hasAppointments);
        model.addAttribute("hasSchedules", hasSchedules);

        return "employees/delete";
    }

    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable("id") Long id, Model model) {
        Optional<Employee> optionalEmployee = employeeService.getById(id);
        if (optionalEmployee.isEmpty()) {
            return "redirect:/employees/all";
        }

        Employee employeeToDelete = optionalEmployee.get();

        boolean hasAppointments = employeeToDelete.getAppointments() != null && !employeeToDelete.getAppointments().isEmpty();
        boolean hasSchedules = employeeToDelete.getSchedules() != null && !employeeToDelete.getSchedules().isEmpty();

        if (hasAppointments || hasSchedules) {
            model.addAttribute("employee", employeeToDelete);
            model.addAttribute("hasAppointments", hasAppointments);
            model.addAttribute("hasSchedules", hasSchedules);
            return "employees/delete";
        }

        employeeService.delete(employeeToDelete.getId());
        return "redirect:/employees/all";
    }

    @GetMapping("/updatePhoto/{id}")
    public String updatePhotoGet(@PathVariable("id") Long id, Model model) {
        Optional<Employee> optionalEmployee = employeeService.getById(id);
        if (optionalEmployee.isEmpty()) {
            return "redirect:/employees/all";
        }
        model.addAttribute("employeeId", id);

        return "employees/updatePhoto";
    }

    @PostMapping("/updatePhoto/{id}")
    public String updatePhotoPost(Model model, @PathVariable("id") Long id, @RequestParam("image") MultipartFile file) {
        Optional<Employee> optionalEmployee = employeeService.getById(id);

        if (optionalEmployee.isEmpty()) {
            return "redirect:/employees/all";
        }

        Employee employee = optionalEmployee.get();
        DbFile dbFile = employee.getMainImage();

        if (dbFile == null){
            dbFile = new DbFile();
        }
        else {
            try {
                FileManager.deleteFile(dbFile.getName());
            } catch (IOException e) {
                return "redirect:/employees/details/" + id;

            }
        }

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
            return "redirect:/employees/details/" + id;
        }

        fileService.update(dbFile);

        employee.setMainImage(dbFile);
        employeeService.update(employee);

        return "redirect:/employees/details/" + id;
    }

    @GetMapping("/changeRole/{id}")
    public String changeRole(@PathVariable("id") Long id) {
        Optional<Employee> optionalEmployee = employeeService.getById(id);
        if (optionalEmployee.isEmpty()) {
            return "redirect:/employees/all";
        }

        Employee employee = optionalEmployee.get();
        User user = employee.getUser();

        if(user.getRole() == Role.ROLE_ADMIN){
            user.setRole(Role.ROLE_OPERATOR);
        }
        else{
            user.setRole(Role.ROLE_ADMIN);
        }

        userService.update(user);

        return "redirect:/employees/details/" + id;
    }

}
