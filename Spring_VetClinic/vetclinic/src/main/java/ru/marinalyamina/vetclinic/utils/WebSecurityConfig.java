package ru.marinalyamina.vetclinic.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    SecurityFilterChain filterChain (HttpSecurity http) throws Exception {
        http.
                authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/img/**", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/vets/**", "/procedures_prices").permitAll()
                        .requestMatchers("/", "/login", "/register", "/public/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/**").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/**").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("USER")

                        .requestMatchers("/animaltypes/create/**").hasRole("ADMIN")
                        .requestMatchers("/animaltypes/update/**").hasRole("ADMIN")
                        .requestMatchers("/animaltypes/delete/**").hasRole("ADMIN")

                        .requestMatchers("/positions/create/**").hasRole("ADMIN")
                        .requestMatchers("/positions/update/**").hasRole("ADMIN")
                        .requestMatchers("/positions/delete/**").hasRole("ADMIN")

                        .requestMatchers("/procedures/create/**").hasRole("ADMIN")
                        .requestMatchers("/procedures/update/**").hasRole("ADMIN")
                        .requestMatchers("/procedures/delete/**").hasRole("ADMIN")

                        .requestMatchers("/employees/create/**").hasRole("ADMIN")
                        .requestMatchers("/employees/update/**").hasRole("ADMIN")
                        .requestMatchers("/employees/delete/**").hasRole("ADMIN")
                        .requestMatchers("/employees/changeRole/**").hasRole("ADMIN")

                        .requestMatchers("/schedules/create/**").hasRole("ADMIN")
                        .requestMatchers("/schedules/delete/**").hasRole("ADMIN")

                        .requestMatchers("/appointments/delete/**").hasRole("ADMIN")

                        //закрыть доступные оператору и админу от клиентов
                        .requestMatchers("/employees/all/**").hasAnyRole("ADMIN", "OPERATOR")
                        //-----------------------------

                        .anyRequest().authenticated())

                .csrf((csrf) -> csrf.disable())
                //.formLogin(Customizer.withDefaults())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        //переход после успешной
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )

                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
