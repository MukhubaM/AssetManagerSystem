package com.assetmanager.AssetManagementSystem.service.impl;

import com.assetmanager.AssetManagementSystem.dto.CreateAssetRequest;
import com.assetmanager.AssetManagementSystem.entity.Asset;
import com.assetmanager.AssetManagementSystem.entity.AssetStatus;
import com.assetmanager.AssetManagementSystem.exception.ResourceNotFoundException;
import com.assetmanager.AssetManagementSystem.repository.AssetRepository;
import com.assetmanager.AssetManagementSystem.security.CurrentUserProvider;
import com.assetmanager.AssetManagementSystem.service.AssetService;
import com.assetmanager.AssetManagementSystem.service.AuditLogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final AuditLogService auditLogService;
    private final CurrentUserProvider currentUserProvider;

    @Value("${upload.directory}")
    private String uploadDirectory;

    @Override
    @Transactional
    public Asset createAsset(CreateAssetRequest request) {

        String photoPath = savePhoto(request.getPhoto());

        Asset asset = Asset.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .serialNumber(request.getSerialNumber())
                .acquisitionDate(request.getAcquisitionDate())
                .cost(request.getCost())
                .location(request.getLocation())
                .condition(request.getCondition())
                .photoPath(photoPath)
                .status(AssetStatus.AVAILABLE)
                .build();

        Asset savedAsset = assetRepository.save(asset);
        auditLogService.log(currentUserProvider.getCurrentUserId(), "ASSET", savedAsset.getAssetId(), "CREATE");

        return savedAsset;
    }

    @Override
    @Transactional
    public Asset updateAsset(Long id, CreateAssetRequest request) {

        Asset asset = getAsset(id);

        asset.setTitle(request.getTitle());
        asset.setCategory(request.getCategory());
        asset.setSerialNumber(request.getSerialNumber());
        asset.setAcquisitionDate(request.getAcquisitionDate());
        asset.setCost(request.getCost());
        asset.setLocation(request.getLocation());
        asset.setCondition(request.getCondition());

        // Only replace the photo if a new one was uploaded, otherwise keeps the existing one
        if (request.getPhoto() != null && !request.getPhoto().isEmpty()) {
            asset.setPhotoPath(savePhoto(request.getPhoto()));
        }

        Asset updatedAsset = assetRepository.save(asset);
        auditLogService.log(currentUserProvider.getCurrentUserId(), "ASSET", updatedAsset.getAssetId(), "UPDATE");

        return updatedAsset;
    }

    @Override
    @Transactional
    public void retireAsset(Long id) {

        Asset asset = getAsset(id);
        asset.setStatus(AssetStatus.RETIRED);
        assetRepository.save(asset);

        auditLogService.log(currentUserProvider.getCurrentUserId(), "ASSET", asset.getAssetId(), "RETIRE");
    }

    @Override
    public Asset getAsset(Long id) {

        return assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + id));
    }

    @Override
    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
    }

    @Override
    public List<Asset> searchByTitle(String title) {
        return assetRepository.findByTitleContainingIgnoreCase(title);
    }

    private String savePhoto(org.springframework.web.multipart.MultipartFile photo) {

        try {
            if (photo == null || photo.isEmpty()) {
                return null;
            }

            Files.createDirectories(Paths.get(uploadDirectory));

            String originalFilename = photo.getOriginalFilename() == null ? "upload" : photo.getOriginalFilename();
            String fileName = UUID.randomUUID() + "_" + originalFilename;

            Path path = Paths.get(uploadDirectory, fileName);
            Files.copy(photo.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            return fileName;

        } catch (IOException e) {
            log.error("Failed to store uploaded photo", e);
            throw new RuntimeException("Photo upload failed", e);
        }
    }
}
