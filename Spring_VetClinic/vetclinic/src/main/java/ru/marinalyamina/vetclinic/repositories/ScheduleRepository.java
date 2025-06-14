package ru.marinalyamina.vetclinic.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.marinalyamina.vetclinic.models.entities.Employee;
import ru.marinalyamina.vetclinic.models.entities.Schedule;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByEmployeeId(Long employeeId);

    @Modifying
    @Transactional
    @Query(
            value = "UPDATE public.schedules SET animal_id = :animalId WHERE id = :scheduleId",
            nativeQuery = true)
    void createAnimalSchedule(Long scheduleId, Long animalId);

    List<Schedule> findByEmployeeAndDateBetweenAndAnimalNotNull(Employee employee, LocalDateTime from, LocalDateTime to);

}

