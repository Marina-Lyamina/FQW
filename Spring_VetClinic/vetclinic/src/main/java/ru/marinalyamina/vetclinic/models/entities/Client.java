package ru.marinalyamina.vetclinic.models.entities;

import jakarta.persistence.*;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.List;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Valid
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private User user;

    @Column(name = "telegram_chat_id")
    private Long telegramChatId;

    @Column(name = "telegram_link_code")
    private String telegramLinkCode;

    @OneToMany(mappedBy = "client")
    @JsonManagedReference
    private List<Animal> animals;
}
