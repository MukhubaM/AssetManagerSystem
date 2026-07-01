package com.assetmanager.AssetManagementSystem.service.impl;

import com.assetmanager.AssetManagementSystem.dto.DashboardStats;
import com.assetmanager.AssetManagementSystem.entity.AssetStatus;
import com.assetmanager.AssetManagementSystem.entity.LoanStatus;
import com.assetmanager.AssetManagementSystem.repository.AssetRepository;
import com.assetmanager.AssetManagementSystem.repository.LoanRepository;
import com.assetmanager.AssetManagementSystem.service.DashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final AssetRepository assetRepository;
    private final LoanRepository loanRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStats getStats() {

        return DashboardStats.builder()
                .totalAssets(assetRepository.count())
                .availableAssets(assetRepository.countByStatus(AssetStatus.AVAILABLE))
                .loanedAssets(assetRepository.countByStatus(AssetStatus.LOANED))
                .pendingLoans(loanRepository.countByStatus(LoanStatus.PENDING))
                .overdueLoans(loanRepository.countByStatus(LoanStatus.OVERDUE))
                .build();
    }
}
