package ru.marinalyamina.vetclinic.apicontrollers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.marinalyamina.vetclinic.models.dtos.LoginDTO;
import ru.marinalyamina.vetclinic.models.dtos.UpdateUserDTO;
import ru.marinalyamina.vetclinic.models.entities.Client;
import ru.marinalyamina.vetclinic.models.entities.User;
import ru.marinalyamina.vetclinic.models.enums.Role;
import ru.marinalyamina.vetclinic.services.ClientService;
import ru.marinalyamina.vetclinic.services.UserService;
import ru.marinalyamina.vetclinic.utils.CurrentUser;

import java.util.Optional;

@RestController
@RequestMapping("/api/clients")
public class ClientApiController {

    private final ClientService clientService;
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    public ClientApiController(ClientService clientService, UserService userService) {
        this.clientService = clientService;
        this.userService = userService;

        passwordEncoder = new BCryptPasswordEncoder();
    }


    @GetMapping("/currentUser")
    public ResponseEntity<Client> getCurrentUser() {
        Long currentClientId = CurrentUser.clientId;

        Optional<Client> clientOptional = clientService.getById(currentClientId);

        if(clientOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(clientOptional.get());
    }

    @PutMapping("/currentUser")
    public ResponseEntity<Client> updateCurrentUser(@Valid @RequestBody UpdateUserDTO userDTO) {
        Long currentClientId = CurrentUser.clientId;

        Optional<Client> clientOptional = clientService.getById(currentClientId);

        if(clientOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var clientUser = clientOptional.get().getUser();

        clientUser.setSurname(userDTO.getSurname());
        clientUser.setName(userDTO.getName());
        clientUser.setPatronymic(userDTO.getPatronymic());
        clientUser.setBirthday(userDTO.getBirthday());
        clientUser.setEmail(userDTO.getEmail());
        clientUser.setPhone(userDTO.getPhone());
        clientUser.setUsername(userDTO.getUsername());

        userService.update(clientUser);

        return ResponseEntity.ok(clientOptional.get());
    }

    @PostMapping("/login")
    public ResponseEntity<Long> login(@Valid @RequestBody LoginDTO loginDTO) {
        Optional<Client> clientOptional = clientService.getByUsername(loginDTO.getUsername());

        if(clientOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if(!passwordEncoder.matches(loginDTO.getPassword(), clientOptional.get().getUser().getPassword())){
            return ResponseEntity.badRequest().build();
        }

        CurrentUser.id = clientOptional.get().getUser().getId();
        CurrentUser.clientId = clientOptional.get().getId();

        return ResponseEntity.ok(clientOptional.get().getUser().getId());
    }

    @PostMapping("/register")
    public ResponseEntity<Long> registration(@Valid @RequestBody User user) {
        Optional<User> userOptional = userService.getByUsername(user.getUsername());

        if(userOptional.isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        // User
        var encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);

        user.setRole(Role.ROLE_USER);

        userService.create(user);

        // Client
        var client = new Client();
        client.setUser(user);

        clientService.create(client);

        return ResponseEntity.ok(client.getId());
    }
}
