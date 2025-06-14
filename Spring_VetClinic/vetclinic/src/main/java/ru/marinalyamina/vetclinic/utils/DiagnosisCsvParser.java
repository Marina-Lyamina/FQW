package ru.marinalyamina.vetclinic.utils;

import org.springframework.web.multipart.MultipartFile;
import ru.marinalyamina.vetclinic.models.entities.Diagnosis;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DiagnosisCsvParser {

    public static List<Diagnosis> parseCSV(MultipartFile file) throws IOException {
        List<Diagnosis> diagnoses = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean isFirstLine = true;
            int nameColumnIndex = -1;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue; // Пропускаем пустые строки
                }

                List<String> fields = parseCsvLine(line);

                if (isFirstLine) {
                    // Ищем колонку с именем диагноза
                    for (int i = 0; i < fields.size(); i++) {
                        String header = fields.get(i).toLowerCase().trim();
                        if (header.equals("name") || header.equals("название") || header.equals("диагноз")) {
                            nameColumnIndex = i;
                            break;
                        }
                    }
                    isFirstLine = false;
                    continue;
                }

                // Извлекаем имя диагноза
                if (nameColumnIndex != -1 && fields.size() > nameColumnIndex) {
                    String name = fields.get(nameColumnIndex).trim();

                    if (!name.isEmpty()) {
                        Diagnosis diagnosis = new Diagnosis();
                        diagnosis.setName(name);
                        // НЕ устанавливаем ID - пусть JPA сам его сгенерирует
                        diagnoses.add(diagnosis);
                    }
                }
            }
        }

        return diagnoses;
    }

    /**
     * Парсит строку CSV, учитывая кавычки и запятые внутри полей
     */
    private static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Двойная кавычка - это экранированная кавычка
                    currentField.append('"');
                    i++; // Пропускаем следующую кавычку
                } else {
                    // Переключаем состояние кавычек
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // Разделитель полей
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }

        // Добавляем последнее поле
        fields.add(currentField.toString());

        return fields;
    }
}