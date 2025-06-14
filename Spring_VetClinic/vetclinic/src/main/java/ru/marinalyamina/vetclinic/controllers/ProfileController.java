package ru.marinalyamina.vetclinic.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.marinalyamina.vetclinic.models.entities.User;
import ru.marinalyamina.vetclinic.models.enums.Role;
import ru.marinalyamina.vetclinic.services.ClientService;
import ru.marinalyamina.vetclinic.services.UserService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final ClientService clientService;

    public ProfileController(UserService userService, ClientService clientService) {
        this.userService = userService;
        this.clientService = clientService;
    }

    @GetMapping
    public String profile(Model model) {
        Optional<User> currentUser = userService.getCurrentUser();
        if (currentUser.isEmpty()) return "redirect:/login";
        //-----tg
        if (currentUser.get().getRole() == Role.ROLE_USER && currentUser.get().getPhone() != null && !currentUser.get().getPhone().isEmpty()) {
            // Показать ссылку на бота
            model.addAttribute("showTelegramBot", true);

            var client = clientService.getAll().stream()
                    .filter(c -> c.getUser().getId().equals(currentUser.get().getId()))
                    .findFirst()
                    .orElseThrow();

            String code = "tg_" + (int)(Math.random() * 1_000_000);
            client.setTelegramLinkCode(code);
            clientService.update(client);

            model.addAttribute("tgCode", code);
        }
        //-------
        model.addAttribute("user", currentUser.get());
        return "auth_clients/profile";
    }

    @PostMapping("/updateEmail")
    public ResponseEntity<?> updateEmail(@RequestBody Map<String, String> payload) {
        String userId = payload.get("userId");
        String email = payload.get("email");

        Optional<User> userOpt = userService.getById(Long.valueOf(userId));
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Пользователь не найден"));
        }

        User user = userOpt.get();

        if (email != null && !email.isBlank()) {
            if (!email.matches("^[\\w-.]+@[\\w-]+\\.[a-z]{2,}$")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Некорректный email"));
            }
            if (!email.equals(user.getEmail()) && userService.existsByEmail(email)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Такой email уже используется"));
            }
            user.setEmail(email);
        } else {
            user.setEmail(null);
        }

        userService.update(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/updatePhone")
    public ResponseEntity<?> updatePhone(@RequestBody Map<String, String> payload) {
        String userId = payload.get("userId");
        String phone = payload.get("phone");

        Optional<User> userOpt = userService.getById(Long.valueOf(userId));
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Пользователь не найден"));
        }

        User user = userOpt.get();

        if (phone != null && !phone.isBlank()) {
            if (!phone.matches("^\\d{11}$")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Телефон должен содержать 11 цифр"));
            }
            if (!phone.equals(user.getPhone()) && userService.existsByPhone(phone)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Такой телефон уже используется"));
            }
            user.setPhone(phone);
        } else {
            user.setPhone(null);
        }

        userService.update(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/updateBirthday")
    public ResponseEntity<?> updateBirthday(@RequestBody Map<String, String> payload) {
        String userId = payload.get("userId");
        String birthdayStr = payload.get("birthday");

        Optional<User> userOpt = userService.getById(Long.valueOf(userId));
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Пользователь не найден"));
        }

        User user = userOpt.get();

        if (birthdayStr != null && !birthdayStr.isBlank()) {
            try {
                LocalDate birthday = LocalDate.parse(birthdayStr);
                user.setBirthday(birthday);
            } catch (DateTimeParseException e) {
                return ResponseEntity.badRequest().body(Map.of("message", "Некорректная дата"));
            }
        } else {
            user.setBirthday(null);
        }

        userService.update(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/updateFio")
    public ResponseEntity<?> updateFio(@RequestBody Map<String, String> payload) {
        String userId = payload.get("userId");
        String surname = payload.get("surname");
        String name = payload.get("name");
        String patronymic = payload.get("patronymic");

        if (surname == null || surname.isBlank() || name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Фамилия и имя обязательны"));
        }

        Optional<User> userOpt = userService.getById(Long.valueOf(userId));
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Пользователь не найден"));
        }

        User user = userOpt.get();
        user.setSurname(surname.trim());
        user.setName(name.trim());
        user.setPatronymic((patronymic != null && !patronymic.isBlank()) ? patronymic.trim() : null);

        userService.update(user);
        return ResponseEntity.ok().build();
    }
}
