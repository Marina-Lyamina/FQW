package ru.marinalyamina.vetclinic.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.marinalyamina.vetclinic.models.dtos.CreateAnimalScheduleDTO;
import ru.marinalyamina.vetclinic.models.dtos.ScheduleDTO;
import ru.marinalyamina.vetclinic.models.entities.Employee;
import ru.marinalyamina.vetclinic.models.entities.Schedule;
import ru.marinalyamina.vetclinic.repositories.ScheduleRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit;


@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    public Optional<Schedule> getById(Long id) {
        return scheduleRepository.findById(id);
    }

    public List<ScheduleDTO> getEmployeeFreeSchedules(Long employeeId) {
        List<Schedule> schedules = scheduleRepository.findByEmployeeId(employeeId);

        return schedules.stream()
                .filter(schedule -> schedule.getAnimal() == null)
                .sorted(Comparator.comparing(Schedule::getDate))
                .map(schedule -> new ScheduleDTO(
                        schedule.getId(),
                        schedule.getDate(),
                        schedule.getEmployee().getId()
                )).collect(Collectors.toList());
    }

    public void createAnimalSchedule(CreateAnimalScheduleDTO animalScheduleDTO) {
        scheduleRepository.createAnimalSchedule(animalScheduleDTO.getScheduleId(), animalScheduleDTO.getAnimalId());
    }

    public Schedule create(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    public boolean existsById(Long id) {
        return scheduleRepository.existsById(id);
    }

    public void delete(Long id) {
        scheduleRepository.deleteById(id);
    }

    public void update(Schedule schedule) {
        scheduleRepository.save(schedule);
    }

    public void createFreeSlot(Employee employee, LocalDateTime dateTime) {
        // Проверка: слот уже есть?
        boolean exists = employee.getSchedules().stream()
                .anyMatch(s -> s.getDate().equals(dateTime));
        if (exists) return;

        Schedule schedule = new Schedule();
        schedule.setEmployee(employee);
        schedule.setDate(dateTime);
        schedule.setAnimal(null); // Свободный слот
        scheduleRepository.save(schedule);
    }


    public void deleteFreeSlotByDate(Employee employee, LocalDateTime dateTime) {
        var slot = employee.getSchedules().stream()
                .filter(s -> s.getAnimal() == null &&
                        s.getDate().truncatedTo(ChronoUnit.MINUTES)
                                .equals(dateTime.truncatedTo(ChronoUnit.MINUTES)))
                .findFirst();
        slot.ifPresent(scheduleRepository::delete);
    }

    public List<Schedule> getAllWithAnimal() {
        return scheduleRepository.findAll().stream()
                .filter(s -> s.getAnimal() != null)
                .toList();
    }

    public List<Schedule> findByEmployeeAndDateBetweenAndAnimalNotNull(Employee employee, LocalDateTime from, LocalDateTime to) {
        return scheduleRepository.findByEmployeeAndDateBetweenAndAnimalNotNull(employee, from, to);
    }

}