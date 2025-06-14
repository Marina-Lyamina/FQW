package ru.marinalyamina.vetclinic.controllers;

import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.marinalyamina.vetclinic.models.entities.Client;
import ru.marinalyamina.vetclinic.models.entities.User;
import ru.marinalyamina.vetclinic.services.ClientService;
import ru.marinalyamina.vetclinic.services.UserService;
import ru.marinalyamina.vetclinic.models.enums.Role;

@Controller
public class RegisterController {

    private final ClientService clientService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public RegisterController(ClientService clientService,
                              UserService userService,
                              PasswordEncoder passwordEncoder) {
        this.clientService = clientService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String registerGet(Model model) {
        model.addAttribute("client", new Client());
        return "register";
    }

    @PostMapping("/register")
    public String registerPost(@ModelAttribute("client") @Valid Client client,
                               BindingResult bindingResult,
                               @RequestParam("confirmPassword") String confirmPassword,
                               Model model) {

        User user = client.getUser();

        if (!user.getPassword().equals(confirmPassword)) {
            bindingResult.rejectValue("user.password", "error.client", "Пароли не совпадают");
        }

        if (userService.existsByPhone(user.getPhone())) {
            bindingResult.rejectValue("user.phone", "error.client", "Такой телефон уже используется");
        }
        if (userService.existsByEmail(user.getEmail())) {
            bindingResult.rejectValue("user.email", "error.client", "Такой email уже используется");
        }
        if (userService.existsByUsername(user.getUsername())) {
            bindingResult.rejectValue("user.username", "error.client", "Логин уже занят");
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }


        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.ROLE_USER);

        userService.create(user);
        clientService.create(client);

        return "redirect:/login";
    }
}
