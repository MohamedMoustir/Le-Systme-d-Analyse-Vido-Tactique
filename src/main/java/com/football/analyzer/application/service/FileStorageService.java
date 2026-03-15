package com.football.analyzer.application.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileStorageService {
    String saveFile(MultipartFile file) ;
    Path loadFile(String filename);
    void deleteFile(String filename);
}
