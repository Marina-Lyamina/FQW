package ru.marinalyamina.vetclinic.controllers;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.marinalyamina.vetclinic.models.entities.User;
import ru.marinalyamina.vetclinic.services.UserService;

import java.util.Optional;

@Controller
@RequestMapping("/users")
public class UserController {

    /*private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/all")
    public String getAll(Model model) {
        model.addAttribute("users", userService.getAll());
        return "users/all";
    }

    @GetMapping("/details/{id}")
    public String details(Model model, @PathVariable("id") Long id) {
        Optional<User> optionalUser = userService.getById(id);
        if (optionalUser.isEmpty()) {
            return "redirect:/users/all";
        }
        model.addAttribute("user", optionalUser.get());
        return "users/details";
    }

    @GetMapping("/create")
    public String createGet(Model model) {
        model.addAttribute("user", new User());
        return "users/create";
    }

    @PostMapping("/create")
    public String createPost(@ModelAttribute("user") @Valid User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "users/create";
        }

        userService.create(user);

        return "redirect:/users/all";
    }

    @GetMapping("/update/{id}")
    public String updateGet(Model model, @PathVariable("id") Long id) {
        Optional<User> optionalUser = userService.getById(id);
        if (optionalUser.isEmpty()) {
            return "redirect:/users/all";
        }
        model.addAttribute("user", optionalUser.get());
        return "users/update";
    }

    @PostMapping("/update")
    public String updatePost(@ModelAttribute("user") @Valid User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "users/update";
        }
        userService.create(user);

        return "redirect:/users/all";
    }

    @GetMapping("/delete/{id}")
    public String deleteGet(Model model, @PathVariable("id") Long id) {
        Optional<User> optionalUser = userService.getById(id);
        if (optionalUser.isEmpty()) {
            return "redirect:/users/all";
        }

        model.addAttribute("user", optionalUser.get());
        return "users/delete";
    }

    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable("id") Long id) {
        if (userService.existsById(id)) {
            userService.delete(id);
        }
        return "redirect:/users/all";
    }*/
}