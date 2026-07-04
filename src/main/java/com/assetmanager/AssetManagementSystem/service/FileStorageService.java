package com.assetmanager.AssetManagementSystem.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    // Saves the file under the given subdirectory
    String store(MultipartFile file, String subdirectory);
}