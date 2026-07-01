package com.assetmanager.AssetManagementSystem.service;

import com.assetmanager.AssetManagementSystem.dto.CreateAssetRequest;
import com.assetmanager.AssetManagementSystem.entity.Asset;

import java.util.List;

public interface AssetService {

    Asset createAsset(CreateAssetRequest request);

    Asset updateAsset(Long id, CreateAssetRequest request);

    void retireAsset(Long id);

    Asset getAsset(Long id);

    List<Asset> getAllAssets();

    List<Asset> searchByTitle(String title);
}
