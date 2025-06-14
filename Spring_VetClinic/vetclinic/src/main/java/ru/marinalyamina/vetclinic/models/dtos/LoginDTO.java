package ru.marinalyamina.vetclinic.models.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoginDTO {

    @NotEmpty(message = "Введите Логин")
    @Size(max = 32, message = "Логин не должен превышать 32 символа")
    private String username;

    @NotEmpty(message = "Введите Пароль")
    @Size(max = 32, message = "Пароль не должен превышать 32 символов")
    private String password;
}
