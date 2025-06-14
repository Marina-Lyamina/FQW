package ru.marinalyamina.vetclinic.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.marinalyamina.vetclinic.models.entities.Client;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByUser_Username(String username);

    Optional<Client> findByTelegramLinkCode(String code);

    Optional<Client> findByTelegramChatId(Long chatId);

    @Query("SELECT c FROM Client c LEFT JOIN FETCH c.animals a WHERE c.telegramChatId = :chatId")
    Optional<Client> findWithAnimalsByTelegramChatId(Long chatId);
}
