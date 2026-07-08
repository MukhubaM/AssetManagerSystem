package com.assetmanager.AssetManagementSystem.service.impl;

import com.assetmanager.AssetManagementSystem.dto.CreateAssetRequest;
import com.assetmanager.AssetManagementSystem.entity.Asset;
import com.assetmanager.AssetManagementSystem.entity.AssetStatus;
import com.assetmanager.AssetManagementSystem.exception.ResourceNotFoundException;
import com.assetmanager.AssetManagementSystem.repository.AssetRepository;
import com.assetmanager.AssetManagementSystem.security.CurrentUserProvider;
import com.assetmanager.AssetManagementSystem.service.AssetService;
import com.assetmanager.AssetManagementSystem.service.AuditLogService;
import com.assetmanager.AssetManagementSystem.service.FileStorageService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private static final String PHOTO_SUBDIRECTORY = "assets";

    private final AssetRepository assetRepository;
    private final AuditLogService auditLogService;
    private final CurrentUserProvider currentUserProvider;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public Asset createAsset(CreateAssetRequest request) {

        String photoPath = fileStorageService.store(request.getPhoto(), PHOTO_SUBDIRECTORY);

        Asset asset = Asset.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .serialNumber(request.getSerialNumber())
                .acquisitionDate(request.getAcquisitionDate())
                .cost(request.getCost())
                .dailyRate(request.getDailyRate())
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
        asset.setDailyRate(request.getDailyRate());
        asset.setLocation(request.getLocation());
        asset.setCondition(request.getCondition());

        // Only replace the photo if a new one was actually uploaded, otherwise it keeps the existing one
        if (request.getPhoto() != null && !request.getPhoto().isEmpty()) {

            asset.setPhotoPath(fileStorageService.store(request.getPhoto(), PHOTO_SUBDIRECTORY));
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

        return assetRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + id));
    }

    @Override
    public List<Asset> getAllAssets() {

        return assetRepository.findAll();
    }

    @Override
    public List<Asset> searchByTitle(String title) {

        return assetRepository.findByTitleContainingIgnoreCase(title);
    }
}