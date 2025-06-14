package ru.marinalyamina.vetclinic.services;

import org.springframework.stereotype.Service;

import ru.marinalyamina.vetclinic.models.entities.Client;
import ru.marinalyamina.vetclinic.repositories.ClientRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<Client> getAll() { return clientRepository.findAll();}

    public Optional<Client> getById(Long id) {
        return clientRepository.findById(id);
    }

    public Optional<Client> getByUsername(String username) {
        return clientRepository.findByUser_Username(username);
    }

    public boolean existsById(Long id) {
        return clientRepository.existsById(id);
    }

    public Client create(Client client) {
        return clientRepository.save(client);
    }

    public void update(Client client) {
        if (clientRepository.existsById(client.getId())) {
            clientRepository.save(client);
        } else {
            throw new IllegalArgumentException("Клиент не найден");
        }
    }

    public void delete(Long id) {
        clientRepository.deleteById(id);
    }

    public Optional<Client> findByTelegramChatId(Long chatId) {
        return clientRepository.findByTelegramChatId(chatId);
    }

    public Optional<Client> findWithAnimalsByTelegramChatId(Long chatId) {
        return clientRepository.findWithAnimalsByTelegramChatId(chatId);
    }

    public Optional<Client> findByTelegramLinkCode(String code) {
        return clientRepository.findByTelegramLinkCode(code);
    }

    public List<Client> findByFioAndPhone(String fio, String phone) {
        List<Client> all = clientRepository.findAll();

        return all.stream()
                .filter(client -> {
                    boolean matchesFio = (fio == null || fio.isBlank()) ||
                            (client.getUser().getSurname() != null && client.getUser().getSurname().toLowerCase().contains(fio.toLowerCase())) ||
                            (client.getUser().getName() != null && client.getUser().getName().toLowerCase().contains(fio.toLowerCase())) ||
                            (client.getUser().getPatronymic() != null && client.getUser().getPatronymic().toLowerCase().contains(fio.toLowerCase()));

                    boolean matchesPhone = (phone == null || phone.isBlank()) ||
                            (client.getUser().getPhone() != null && client.getUser().getPhone().contains(phone));

                    return matchesFio && matchesPhone;
                })
                .collect(Collectors.toList());
    }


}
