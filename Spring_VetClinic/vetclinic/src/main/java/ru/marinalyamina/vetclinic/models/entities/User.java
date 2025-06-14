package ru.marinalyamina.vetclinic.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.marinalyamina.vetclinic.models.enums.Role;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 32, nullable = false)
    @NotEmpty(message = "Введите фамилию")
    @Size(max = 32, message = "Фамилия не должна превышать 32 символа")
    private String surname;

    @Column(length = 32, nullable = false)
    @NotEmpty(message = "Введите имя")
    @Size(max = 32, message = "Имя не должно превышать 32 символа")
    private String name;

    @Column(length = 32)
    @Size(max = 32, message = "Отчество не должно превышать 32 символа")
    private String patronymic;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, fallbackPatterns = {"dd.MM.yyyy"})
    private LocalDate birthday;

    @Column(length = 128, unique = true)
    @Email(message = "Некорректный ввод Email")
    @Size(max = 128, message = "Email не должен превышать 128 символов")
    private String email;

    @Column(length = 11, unique = true)
    @Pattern(regexp = "^[0-9]*$", message = "Номер телефона может включать только цифры")
    @Size(max = 11, message = "Номер телефона не должен превышать 11 цифр")
    private String phone;

    /*@Column(length = 11, unique = true)
    @Pattern(regexp = "^\\d{11}$", message = "Номер телефона должен содержать ровно 11 цифр")
    private String phone;

    public String normalizePhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return null;
        }
        phone = phone.replaceAll("\\D", "");
        if (phone.startsWith("7") && phone.length() == 11) {
            phone = "8" + phone.substring(1);
        }
        return phone.length() == 11 ? phone : null;
    }*/

    @Column(length = 32, unique = true, nullable = false)
    @NotEmpty(message = "Введите логин")
    @Size(max = 32, message = "Логин не должен превышать 32 символа")
    private String username;

    @Column(length = 256, nullable = false)
    @NotEmpty(message = "Введите пароль")
    @Size(max = 256, message = "Пароль не должен превышать 256 символов")
    private String password;


    public String getFIO() {
        return surname + " " + name + (patronymic != null ? " " + patronymic : "");
    }

    @Enumerated(EnumType.STRING)
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    public String getFIOAbbreviated() {
        StringBuilder sb = new StringBuilder();
        if (surname != null && !surname.isEmpty()) {
            sb.append(surname).append(" ");
        }
        if (name != null && !name.isEmpty()) {
            sb.append(name.charAt(0)).append(".");
        }
        if (patronymic != null && !patronymic.isEmpty()) {
            sb.append(patronymic.charAt(0)).append(".");
        }
        return sb.toString().trim();
    }


}
