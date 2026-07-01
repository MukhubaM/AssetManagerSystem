package com.assetmanager.AssetManagementSystem.repository;

import com.assetmanager.AssetManagementSystem.entity.Asset;
import com.assetmanager.AssetManagementSystem.entity.AssetStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {


    // Search engine   //develop these methods
    List<Asset> findByTitleContainingIgnoreCase(String title);

    List<Asset> findByCategoryContainingIgnoreCase(String category);

    List<Asset> findByLocationContainingIgnoreCase(String location);

    List<Asset> findByStatus(AssetStatus status);

    long countByStatus(AssetStatus status);
}
