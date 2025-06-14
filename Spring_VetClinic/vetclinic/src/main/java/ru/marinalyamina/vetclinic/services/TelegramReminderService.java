package ru.marinalyamina.vetclinic.services;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.marinalyamina.vetclinic.models.VetClinicBot;
import ru.marinalyamina.vetclinic.models.entities.Schedule;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TelegramReminderService {

    private final Object lock = new Object();
    private final ScheduleService scheduleService;
    private final VetClinicBot vetClinicBot;

    // Множества для отслеживания уже отправленных напоминаний
    private final Set<String> sentHourReminders = new HashSet<>();
    private final Set<String> sentDayReminders = new HashSet<>();

    @Scheduled(fixedRate = 60000) // каждую минуту
    public void checkUpcomingAppointments() {
        synchronized (lock) {
            LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
            LocalDateTime inOneHour = now.plusHours(1);
            LocalDateTime inOneDay = now.plusDays(1);

            List<Schedule> schedules = scheduleService.getAllWithAnimal();

            for (Schedule schedule : schedules) {
                LocalDateTime visitTime = schedule.getDate().withSecond(0).withNano(0);
                String scheduleKey = generateScheduleKey(schedule);

                // Проверяем напоминание за час (точное время)
                if (visitTime.equals(inOneHour)) {
                    if (!sentHourReminders.contains(scheduleKey)) {
                        sendReminder(schedule, "⏰ Напоминание: Прием через 1 час");
                        sentHourReminders.add(scheduleKey);
                    }
                }

                // Проверяем напоминание за день (точное время)
                else if (visitTime.equals(inOneDay)) {
                    if (!sentDayReminders.contains(scheduleKey)) {
                        sendReminder(schedule, "📅 Напоминание: Прием завтра");
                        sentDayReminders.add(scheduleKey);
                    }
                }
            }

            // Очищаем старые записи (старше 2 дней)
            cleanupOldReminders();
        }
    }

    private String generateScheduleKey(Schedule schedule) {
        return schedule.getId() + "_" + schedule.getDate().toString();
    }

    // Очищаем старые записи каждый час
    @Scheduled(fixedRate = 3600000)
    public void cleanupOldReminders() {
        synchronized (lock) {
            LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);

            // Удаляем записи о напоминаниях для визитов старше 2 дней
            sentHourReminders.removeIf(key -> {
                try {
                    String dateStr = key.split("_", 2)[1];
                    LocalDateTime scheduleDate = LocalDateTime.parse(dateStr);
                    return scheduleDate.isBefore(twoDaysAgo);
                } catch (Exception e) {
                    return true; // Удаляем некорректные записи
                }
            });

            sentDayReminders.removeIf(key -> {
                try {
                    String dateStr = key.split("_", 2)[1];
                    LocalDateTime scheduleDate = LocalDateTime.parse(dateStr);
                    return scheduleDate.isBefore(twoDaysAgo);
                } catch (Exception e) {
                    return true; // Удаляем некорректные записи
                }
            });
        }
    }

    private void sendReminder(Schedule schedule, String prefix) {
        var animal = schedule.getAnimal();
        if (animal == null) return;

        var client = animal.getClient();
        if (client == null || client.getTelegramChatId() == null) return;

        var employee = schedule.getEmployee();
        var employeeUser = employee.getUser();

        String formattedDateTime = schedule.getDate()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy 'в' HH:mm"));

        String message = prefix + "\n" +
                "📅 Дата и время: " + formattedDateTime + "\n" +
                "🐾 Питомец: " + animal.getName() + "\n" +
                "👨‍⚕️ Врач: " + employeeUser.getFIOAbbreviated() + " (" + employee.getPosition().getName() + ")";

        vetClinicBot.sendMessageToChat(client.getTelegramChatId(), message);
    }


}