package ru.marinalyamina.vetclinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import ru.marinalyamina.vetclinic.utils.FileManager;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.marinalyamina.vetclinic.models.VetClinicBot;

@EnableAutoConfiguration
@SpringBootApplication
@EnableScheduling
public class VetclinicApplication {

	public static void main(String[] args) {
		SpringApplication.run(VetclinicApplication.class, args);

		FileManager.checkRootDir();
	}

	@Bean
	public TelegramBotsApi telegramBotsApi(VetClinicBot vetClinicBot) throws TelegramApiException {
		TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
		api.registerBot(vetClinicBot);
		System.out.println("=== БОТ ЗАРЕГИСТРИРОВАН ===");
		return api;
	}



}
