package ru.marinalyamina.vetclinic.models;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.marinalyamina.vetclinic.models.dtos.CreateAnimalScheduleDTO;
import ru.marinalyamina.vetclinic.models.dtos.ScheduleDTO;
import ru.marinalyamina.vetclinic.models.entities.*;
import ru.marinalyamina.vetclinic.services.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VetClinicBot extends TelegramLongPollingBot {

    private final ClientService clientService;
    private final EmployeeService employeeService;
    private final AnimalService animalService;
    private final ScheduleService scheduleService;

    // Хранение данных выбора слота расписания для каждого пользователя
    private final Map<Long, ScheduleData> schedulingSessions = new ConcurrentHashMap<>();

    public VetClinicBot(ClientService clientService, EmployeeService employeeService,
                        AnimalService animalService, ScheduleService scheduleService) {
        this.clientService = clientService;
        this.employeeService = employeeService;
        this.animalService = animalService;
        this.scheduleService = scheduleService;
    }

    @Value("${telegrambots.bots[0].token}")
    private String botToken;

    @Value("${telegrambots.bots[0].username}")
    private String botUsername;

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @PostConstruct
    public void init() {
        System.out.println("=== VetClinicBot инициализирован ===");
        System.out.println("Bot username: " + botUsername);
        System.out.println("Bot token: " + (botToken != null ? "есть" : "null"));
        System.out.println("===============================");
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleTextMessage(update);
            } else if (update.hasCallbackQuery()) {
                handleCallbackQuery(update);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при обработке обновления: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleTextMessage(Update update) {
        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        if (messageText.startsWith("/код")) {
            handleTelegramLinking(messageText, chatId);
        } else if (messageText.equals("/start")) {
            handleStart(chatId);
        } else if (messageText.equals("/запись")) {
            startSchedulingProcess(chatId);
        } else {
            // Если аккаунт не привязан
            if (!isClientLinked(chatId)) {
                sendMessage(chatId, "❌ Сначала привяжите свой аккаунт с помощью команды /код ВАШ_КОД");
                return;
            }
            sendMessage(chatId, "Используйте /запись для записи на прием");
        }
    }

    private void handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        System.out.println("Получен callback: " + callbackData + " от пользователя: " + chatId);

        try {
            if (callbackData.startsWith("doctor_")) {
                removeInlineKeyboard(update.getCallbackQuery());
                long doctorId = Long.parseLong(callbackData.replace("doctor_", ""));
                selectDoctor(chatId, doctorId);
            } else if (callbackData.startsWith("date_")) {
                removeInlineKeyboard(update.getCallbackQuery());
                String dateStr = callbackData.replace("date_", "");
                selectDate(chatId, dateStr);
            } else if (callbackData.startsWith("time_")) {
                removeInlineKeyboard(update.getCallbackQuery());
                long scheduleId = Long.parseLong(callbackData.replace("time_", ""));
                selectTimeSlot(chatId, scheduleId);
            } else if (callbackData.startsWith("pet_")) {
                removeInlineKeyboard(update.getCallbackQuery());
                long petId = Long.parseLong(callbackData.replace("pet_", ""));
                selectPet(chatId, petId);
            } else if (callbackData.equals("confirm_yes")) {
                removeInlineKeyboard(update.getCallbackQuery());
                confirmScheduling(chatId);
            } else if (callbackData.equals("confirm_no")) {
                removeInlineKeyboard(update.getCallbackQuery());
                cancelScheduling(chatId);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при обработке callback: " + e.getMessage());
            e.printStackTrace();
            sendMessage(chatId, "❌ Произошла ошибка. Попробуйте начать заново с команды /запись");
        }
    }

    private void handleTelegramLinking(String messageText, long chatId) {
        String code = messageText.substring(4).trim();

        Optional<Client> clientOpt = clientService.findByTelegramLinkCode(code);
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            client.setTelegramChatId(chatId);
            client.setTelegramLinkCode(null);
            clientService.update(client);

            sendMessage(chatId, "✅ Вы успешно привязали Telegram к своему аккаунту!\n\n" +
                    "Теперь вы можете записаться на прием с помощью команды /запись");
        } else {
            sendMessage(chatId, "❌ Код не найден. Убедитесь, что вы ввели его правильно.");
        }
    }

    private void handleStart(long chatId) {
        if (isClientLinked(chatId)) {
            sendMessage(chatId, "👋 Добро пожаловать в ветеринарную клинику!\n\n" +
                    "Для записи на прием используйте команду /запись");
        } else {
            sendMessage(chatId, "👋 Добро пожаловать в ветеринарную клинику!\n\n" +
                    "Для начала работы привяжите свой аккаунт с помощью команды:\n" +
                    "/код ВАШ_КОД\n\n" +
                    "Код можно получить в личном кабинете на сайте.");
        }
    }

    private void startSchedulingProcess(long chatId) {
        if (!isClientLinked(chatId)) {
            sendMessage(chatId, "❌ Сначала привяжите свой аккаунт с помощью команды /код ВАШ_КОД");
            return;
        }

        // Инициализируем новую сессию выбора слота расписания
        schedulingSessions.put(chatId, new ScheduleData());

        // Показываем список врачей
        showDoctors(chatId);
    }

    private void showDoctors(long chatId) {
        List<Employee> doctors = employeeService.getAll();

        if (doctors.isEmpty()) {
            sendMessage(chatId, "❌ В данный момент нет доступных врачей.");
            return;
        }

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("👨‍⚕️ Выберите врача:");

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Employee doctor : doctors) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(doctor.getUser().getFIO() + " - " + doctor.getPosition().getName());
            button.setCallbackData("doctor_" + doctor.getId());
            row.add(button);
            rows.add(row);
        }

        keyboard.setKeyboard(rows);
        message.setReplyMarkup(keyboard);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void selectDoctor(long chatId, long doctorId) {
        ScheduleData scheduleData = schedulingSessions.get(chatId);
        if (scheduleData == null) {
            sendMessage(chatId, "❌ Ошибка. Начните запись заново с команды /запись");
            return;
        }

        Optional<Employee> doctorOpt = employeeService.getById(doctorId);
        if (doctorOpt.isEmpty()) {
            sendMessage(chatId, "❌ Врач не найден.");
            return;
        }

        scheduleData.setDoctor(doctorOpt.get());
        showAvailableDates(chatId, doctorId);
    }

    private void showAvailableDates(long chatId, long doctorId) {
        // Получаем свободные слоты врача
        List<ScheduleDTO> schedules = scheduleService.getEmployeeFreeSchedules(doctorId);

        if (schedules.isEmpty()) {
            sendMessage(chatId, "❌ У выбранного врача нет свободных слотов в ближайшие 7 дней.");
            return;
        }

        // Получаем уникальные даты из слотов (только ближайшие 7 дней)
        LocalDate today = LocalDate.now();
        Set<LocalDate> availableDates = new HashSet<>();

        for (ScheduleDTO schedule : schedules) {
            LocalDate scheduleDate = schedule.getDate().toLocalDate();
            if (!scheduleDate.isBefore(today) && scheduleDate.isBefore(today.plusDays(8))) {
                availableDates.add(scheduleDate);
            }
        }

        if (availableDates.isEmpty()) {
            sendMessage(chatId, "❌ Нет свободных слотов в ближайшие 7 дней.");
            return;
        }

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("📅 Выберите дату:");

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<LocalDate> sortedDates = new ArrayList<>(availableDates);
        sortedDates.sort(LocalDate::compareTo);

        for (LocalDate date : sortedDates) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();

            String dateText = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            if (date.equals(today)) {
                dateText += " (сегодня)";
            } else if (date.equals(today.plusDays(1))) {
                dateText += " (завтра)";
            }

            button.setText(dateText);
            button.setCallbackData("date_" + date.toString());
            row.add(button);
            rows.add(row);
        }

        keyboard.setKeyboard(rows);
        message.setReplyMarkup(keyboard);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void selectDate(long chatId, String dateStr) {
        ScheduleData scheduleData = schedulingSessions.get(chatId);
        if (scheduleData == null || scheduleData.getDoctor() == null) {
            sendMessage(chatId, "❌ Ошибка. Начните запись заново с команды /запись");
            return;
        }

        LocalDate selectedDate = LocalDate.parse(dateStr);
        scheduleData.setSelectedDate(selectedDate);

        showAvailableTimeSlots(chatId, scheduleData.getDoctor().getId(), selectedDate);
    }

    private void showAvailableTimeSlots(long chatId, long doctorId, LocalDate date) {
        List<ScheduleDTO> schedules = scheduleService.getEmployeeFreeSchedules(doctorId);

        // Фильтруем по выбранной дате
        List<ScheduleDTO> dateSchedules = schedules.stream()
                .filter(s -> s.getDate().toLocalDate().equals(date))
                .sorted((s1, s2) -> s1.getDate().compareTo(s2.getDate()))
                .toList();

        if (dateSchedules.isEmpty()) {
            sendMessage(chatId, "❌ На выбранную дату нет свободных слотов.");
            return;
        }

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("🕐 Выберите время:");

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (ScheduleDTO schedule : dateSchedules) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();

            String timeText = schedule.getDate().format(DateTimeFormatter.ofPattern("HH:mm"));
            button.setText(timeText);
            button.setCallbackData("time_" + schedule.getId());
            row.add(button);
            rows.add(row);
        }

        keyboard.setKeyboard(rows);
        message.setReplyMarkup(keyboard);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void selectTimeSlot(long chatId, long scheduleId) {
        System.out.println("selectTimeSlot: chatId=" + chatId + ", scheduleId=" + scheduleId);

        System.out.println("== ВЫБОР ВРЕМЕНИ ===");
        System.out.println("chatId: " + chatId);
        System.out.println("scheduleId: " + scheduleId);
        System.out.println("schedulingSessions: " + schedulingSessions.keySet());


        ScheduleData scheduleData = schedulingSessions.get(chatId);
        if (scheduleData == null) {
            System.out.println("scheduleData is null!");
            sendMessage(chatId, "❌ Ошибка. Начните запись заново с команды /запись");
            return;
        }

        // Сохраняем выбранный слот и сразу переходим к выбору питомца
        scheduleData.setScheduleId(scheduleId);
        System.out.println("scheduleId установлен: " + scheduleId + ", переходим к выбору питомца");
        showPetSelection(chatId);
    }

    private void showPetSelection(long chatId) {
        System.out.println("showPetSelection вызван для chatId: " + chatId);

        Client client = clientService.findWithAnimalsByTelegramChatId(chatId).orElse(null);
        if (client == null) {
            sendMessage(chatId, "❌ Не удалось загрузить клиента с питомцами.");
            return;
        }

        List<Animal> pets = client.getAnimals();

        System.out.println("Найдено питомцев для клиента: " + pets.size());

        if (pets.isEmpty()) {
            sendMessage(chatId, "❌ У вас нет зарегистрированных питомцев. Обратитесь в клинику для регистрации.");
            return;
        }

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("🐾 Выберите питомца:");

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Animal pet : pets) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(pet.getName());
            button.setCallbackData("pet_" + pet.getId());
            row.add(button);
            rows.add(row);
        }

        keyboard.setKeyboard(rows);
        message.setReplyMarkup(keyboard);

        try {
            execute(message);
            System.out.println("Сообщение с выбором питомцев отправлено");
        } catch (TelegramApiException e) {
            System.err.println("Ошибка отправки сообщения с питомцами: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void selectPet(long chatId, long petId) {
        ScheduleData scheduleData = schedulingSessions.get(chatId);
        if (scheduleData == null) {
            sendMessage(chatId, "❌ Ошибка. Начните запись заново с команды /запись");
            return;
        }

        Optional<Animal> petOpt = animalService.findWithSchedulesById(petId);
        if (petOpt.isEmpty()) {
            sendMessage(chatId, "❌ Питомец не найден.");
            return;
        }

        scheduleData.setPet(petOpt.get());
        showConfirmation(chatId);
    }

    private void showConfirmation(long chatId) {
        ScheduleData scheduleData = schedulingSessions.get(chatId);
        if (scheduleData == null || !scheduleData.isComplete()) {
            sendMessage(chatId, "❌ Ошибка данных записи. Начните заново с команды /запись");
            return;
        }

        // Получаем точное время из слота
        Optional<Schedule> scheduleOpt = scheduleService.getById(scheduleData.getScheduleId());
        if (scheduleOpt.isEmpty()) {
            sendMessage(chatId, "❌ Не удалось получить информацию о времени приема.");
            return;
        }

        String dateTimeStr = scheduleOpt.get().getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

        String confirmText = "✅ Подтверждение записи:\n\n" +
                "👨‍⚕️ Врач: " + scheduleData.getDoctor().getUser().getFIO() + "\n" +
                "📅 Дата и время: " + dateTimeStr + "\n" +
                "🐾 Питомец: " + scheduleData.getPet().getName() + "\n\n" +
                "Подтвердить запись?";

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(confirmText);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton yesButton = new InlineKeyboardButton();
        yesButton.setText("✅ Подтвердить");
        yesButton.setCallbackData("confirm_yes");

        InlineKeyboardButton noButton = new InlineKeyboardButton();
        noButton.setText("❌ Отменить");
        noButton.setCallbackData("confirm_no");

        row.add(yesButton);
        row.add(noButton);
        rows.add(row);

        keyboard.setKeyboard(rows);
        message.setReplyMarkup(keyboard);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Ошибка при показе подтверждения: " + e.getMessage());
            e.printStackTrace();
            sendMessage(chatId, "❌ Ошибка при подготовке подтверждения. Попробуйте еще раз.");
        }
    }


    private void confirmScheduling(long chatId) {
        ScheduleData scheduleData = schedulingSessions.get(chatId);
        if (scheduleData == null || !scheduleData.isComplete()) {
            sendMessage(chatId, "❌ Ошибка данных записи.");
            return;
        }

        try {
            // Создаем запись
            CreateAnimalScheduleDTO dto = new CreateAnimalScheduleDTO();
            dto.setAnimalId(scheduleData.getPet().getId());
            dto.setScheduleId(scheduleData.getScheduleId());

            scheduleService.createAnimalSchedule(dto);

            // Получаем точное время приема
            Schedule schedule = scheduleService.getById(scheduleData.getScheduleId())
                    .orElseThrow(() -> new RuntimeException("Не найден слот расписания"));

            String dateTimeStr = schedule.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

            String confirmationText = "✅ Запись успешно создана!\n\n" +
                    "👨‍⚕️ Врач: " + scheduleData.getDoctor().getUser().getFIO() + "\n" +
                    "📅 Дата и время: " + dateTimeStr + "\n" +
                    "🐾 Питомец: " + scheduleData.getPet().getName() + "\n\n" +
                    "Мы отправим вам напоминание за день и за час до приема.";

            sendMessage(chatId, confirmationText);
            schedulingSessions.remove(chatId);

        } catch (Exception e) {
            sendMessage(chatId, "❌ Ошибка при создании записи: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void cancelScheduling(long chatId) {
        schedulingSessions.remove(chatId);
        sendMessage(chatId, "❌ Запись отменена.");
    }

    private boolean isClientLinked(long chatId) {
        return clientService.findByTelegramChatId(chatId).isPresent();
    }

    private Client getClientByChatId(long chatId) {
        return clientService.findByTelegramChatId(chatId).orElse(null);
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Ошибка отправки сообщения: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendMessageToChat(Long chatId, String text) {
        if (chatId == null) return;
        sendMessage(chatId, text);
    }

    private void removeInlineKeyboard(CallbackQuery callbackQuery) {
        EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup();
        editMarkup.setChatId(callbackQuery.getMessage().getChatId().toString());
        editMarkup.setMessageId(callbackQuery.getMessage().getMessageId());
        editMarkup.setReplyMarkup(null); // удаляем клавиатуру

        try {
            execute(editMarkup);
        } catch (TelegramApiException e) {
            System.err.println("Не удалось удалить клавиатуру: " + e.getMessage());
        }
    }


    // Класс для хранения данных выбора слота расписания
    private static class ScheduleData {
        private Employee doctor;
        private LocalDate selectedDate;
        private Long scheduleId;
        private Animal pet;

        public boolean isComplete() {
            return doctor != null && selectedDate != null && scheduleId != null && pet != null;
        }

        // Геттеры и сеттеры
        public Employee getDoctor() { return doctor; }
        public void setDoctor(Employee doctor) { this.doctor = doctor; }

        public LocalDate getSelectedDate() { return selectedDate; }
        public void setSelectedDate(LocalDate selectedDate) { this.selectedDate = selectedDate; }

        public Long getScheduleId() { return scheduleId; }
        public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

        public Animal getPet() { return pet; }
        public void setPet(Animal pet) { this.pet = pet; }
    }
}