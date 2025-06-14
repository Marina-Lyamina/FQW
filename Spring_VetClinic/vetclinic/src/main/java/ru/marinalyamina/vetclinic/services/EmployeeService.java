package ru.marinalyamina.vetclinic.services;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import ru.marinalyamina.vetclinic.models.entities.Employee;
import ru.marinalyamina.vetclinic.repositories.EmployeeRepository;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<Employee> getAll() { return employeeRepository.findAll();}

    public Optional<Employee> getById(Long id) {
        return employeeRepository.findById(id);
    }

    public boolean existsById(Long id) {
        return employeeRepository.existsById(id);
    }

    public Employee create(Employee employee) {
        if (employee.getDescription().isBlank()) {
            employee.setDescription(null);
        }
        return employeeRepository.save(employee);
    }

    public void update(Employee employee) {
        if (employeeRepository.existsById(employee.getId())) {
            if (employee.getDescription().isBlank()) {
                employee.setDescription(null);
            }
            employeeRepository.save(employee);
        } else {
            throw new IllegalArgumentException("Сотрудник не найден");
        }

    }

    public void delete(Long id) {
        employeeRepository.deleteById(id);
    }

    public List<Employee> findByFilters(String fio, String position, String status) {
        return employeeRepository.findAll().stream()
                .filter(e -> fio == null || e.getUser().getFIO().toLowerCase().contains(fio.toLowerCase()))
                .filter(e -> position == null || position.isEmpty() || e.getPosition().getName().equals(position))
                .filter(e -> {
                    if (status == null || status.isEmpty()) return true;
                    return status.equals("active") ? e.isActive() : !e.isActive();
                })
                .toList();
    }

    public void deactivate(Long id) {
        Employee employee = employeeRepository.findById(id).orElseThrow();
        employee.setActive(false);
        employeeRepository.save(employee);
    }


    public void toggleActive(Long id) {
        Employee employee = getById(id).orElseThrow();
        employee.setActive(!employee.isActive());
        employeeRepository.save(employee);
    }

    public void toggleOnline(Long id) {
        Employee employee = getById(id).orElseThrow();
        employee.setAvailableForOnline(!employee.isAvailableForOnline());
        employeeRepository.save(employee);
    }


}
