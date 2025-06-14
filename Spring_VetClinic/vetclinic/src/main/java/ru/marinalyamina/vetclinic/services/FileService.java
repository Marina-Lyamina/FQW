package ru.marinalyamina.vetclinic.services;

import org.springframework.stereotype.Service;
import ru.marinalyamina.vetclinic.models.entities.DbFile;
import ru.marinalyamina.vetclinic.repositories.FileRepository;

@Service
public class FileService {
    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public DbFile update(DbFile dbFile) {
        return fileRepository.save(dbFile);
    }
}
