package ru.marinalyamina.vetclinic.services;

import org.springframework.stereotype.Service;

import ru.marinalyamina.vetclinic.models.entities.Appointment;
import ru.marinalyamina.vetclinic.models.entities.Employee;
import ru.marinalyamina.vetclinic.models.entities.User;
import ru.marinalyamina.vetclinic.repositories.AppointmentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public List<Appointment> getAll() { return appointmentRepository.findAll();}

    public Optional<Appointment> getById(Long id) {
        return appointmentRepository.findById(id);
    }

    public boolean existsById(Long id) {
        return appointmentRepository.existsById(id);
    }

    public Appointment create(Appointment appointment) {
        cleanEmptyFields(appointment);
        return appointmentRepository.save(appointment);
    }

    public void delete(Long id) {
        appointmentRepository.deleteById(id);
    }

    public void update(Appointment appointment) {
        cleanEmptyFields(appointment);
        appointmentRepository.save(appointment);
    }

    private void cleanEmptyFields(Appointment appointment) {
        if (appointment.getReason() != null && appointment.getReason().isBlank()) {
            appointment.setReason(null);
        }
        /*if (appointment.getDiagnosis() != null && appointment.getDiagnosis().isBlank()) {
            appointment.setDiagnosis(null);
        }*/
        if (appointment.getMedicalPrescription() != null && appointment.getMedicalPrescription().isBlank()) {
            appointment.setMedicalPrescription(null);
        }
    }

    public List<Appointment> findByEmployeeAndEndDateIsNull(Employee employee) {
        return appointmentRepository.findByEmployeesContainingAndEndDateIsNull(employee);
    }

    public List<Appointment> findByEmployeeAndEndDateBetween(Employee employee, LocalDateTime from, LocalDateTime to) {
        return appointmentRepository.findByEmployeesContainingAndEndDateBetween(employee, from, to);
    }

    public void completeAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        appointment.completeAppointment();
        appointmentRepository.save(appointment);
    }


}
