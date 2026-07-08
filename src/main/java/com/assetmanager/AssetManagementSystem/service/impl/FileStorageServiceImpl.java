package com.assetmanager.AssetManagementSystem.service.impl;

import com.assetmanager.AssetManagementSystem.service.FileStorageService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${upload.directory}")
    private String uploadDirectory;

    @Override
    public String store(MultipartFile file, String subdirectory) {

        try {
            if (file == null || file.isEmpty()) {

                return null;
            }

            Path directory = Paths.get(uploadDirectory, subdirectory);
            Files.createDirectories(directory);

            String originalFilename = file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename();
            String fileName = UUID.randomUUID() + "_" + originalFilename;

            Path path = directory.resolve(fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            return subdirectory + "/" + fileName;

        } catch (IOException e) {
            log.error("Failed to store uploaded file in {}", subdirectory, e);
            throw new RuntimeException("File upload failed", e);
        }
    }
}