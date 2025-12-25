package com.savt.backend.infrastructure.service;

import com.savt.backend.application.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl  implements FileStorageService {

    private final Path rootLocation = Paths.get("uploads/videos");
    public FileStorageServiceImpl(){
        try{
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);

        }
    }
    @Override
    public String saveFile(MultipartFile file)  {
        try {
            if (file.isEmpty()) throw new RuntimeException("Failed to store empty file.");

            String extension = getFileExtension(file.getOriginalFilename());
            String fileName = UUID.randomUUID().toString() + extension;

            Path destinationFile = this.rootLocation.resolve(Paths.get(fileName))
                    .normalize().toAbsolutePath();

            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    @Override
    public Path loadFile(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public void deleteFile(String filename) {
         try{
             Files.deleteIfExists( rootLocation.resolve(filename));
         } catch (Exception e) {
             throw new RuntimeException("Error: " + e.getMessage());
         }
    }
    private String getFileExtension(String fileName) {
        if (fileName == null) return ".mp4";
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
