package ru.marinalyamina.vetclinic.utils;

import org.apache.tomcat.util.codec.binary.Base64;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileManager {
    //добавлен \vetclinic\
    public static String rootDir = System.getProperty("user.dir") + "\\vetclinic\\files";
    public static String rootBaseDir = System.getProperty("user.dir") + "\\vetclinic\\baseFiles";


    public static void checkRootDir() {
        File dir = new File(rootDir);

        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static String createFileName(String fileExtensions){
        if (!fileExtensions.startsWith(".")){
            fileExtensions = "." + fileExtensions;
        }
        return UUID.randomUUID() + fileExtensions;
    }

    public static void saveFile(String fileName, byte[] fileContent) throws IOException {
        Path filePath = Paths.get(rootDir, fileName);
        Files.write(filePath, fileContent);
    }

    public static void saveFile(String fileName, String fileContent) throws IOException {
        Path filePath = Paths.get(rootDir, fileName);
        Files.write(filePath, Base64.decodeBase64(fileContent));
    }

    public static String getFile(String fileName) throws IOException {
        Path filePath = Paths.get(rootDir, fileName);

        File file = new File(filePath.toUri());
        byte[] fileContent = Files.readAllBytes(file.toPath());

        return Base64.encodeBase64String(fileContent);
    }

    public static void deleteFile(String fileName) throws IOException {
        Path filePath = Paths.get(rootDir, fileName);
        File file = new File(filePath.toUri());
        file.delete();
    }

    public static String getBaseFile(String fileName) throws IOException {
        Path filePath = Paths.get(rootBaseDir, fileName);

        File file = new File(filePath.toUri());
        byte[] fileContent = Files.readAllBytes(file.toPath());

        return Base64.encodeBase64String(fileContent);
    }
}
