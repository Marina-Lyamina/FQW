package ru.marinalyamina.vetclinic.services;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import ru.marinalyamina.vetclinic.models.entities.User;
import ru.marinalyamina.vetclinic.repositories.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAll() { return userRepository.findAll();}

    public Optional<User> getById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getCurrentUser() {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        return getByUsername(username);
    }

    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public User create(User user) {
        cleanEmptyFields(user);
        return userRepository.save(user);
    }

    public void update(User user) {
        if (userRepository.existsById(user.getId())) {
            cleanEmptyFields(user);
            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("Пользователь не найден");
        }
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }


    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return getByUsername(username)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        Collections.singleton(user.getRole())
                ))
                .orElseThrow(() -> new UsernameNotFoundException("Невозможно найти пользователя" + username));
    }

    /*private void cleanEmptyFields(User user) {
        if (user.getEmail().isBlank()) {
            user.setEmail(null);
        }
        if (user.getPhone().isBlank()) {
            user.setPhone(null);
        }
        if (user.getPatronymic().isBlank()) {
            user.setPatronymic(null);
        }
    }*/

    private void cleanEmptyFields(User user) {
        if (user.getEmail() != null && user.getEmail().isBlank()) {
            user.setEmail(null);
        }
        if (user.getPhone() != null && user.getPhone().isBlank()) {
            user.setPhone(null);
        }
        if (user.getPatronymic() != null && user.getPatronymic().isBlank()) {
            user.setPatronymic(null);
        }
    }

}
