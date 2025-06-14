package ru.marinalyamina.vetclinic.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.marinalyamina.vetclinic.models.entities.Appointment;
import ru.marinalyamina.vetclinic.models.entities.Employee;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByEmployeesContainingAndEndDateIsNull(Employee employee);

    List<Appointment> findByEmployeesContainingAndEndDateBetween(Employee employee, LocalDateTime from, LocalDateTime to);

}
