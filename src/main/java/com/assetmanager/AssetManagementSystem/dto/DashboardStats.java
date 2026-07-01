package com.assetmanager.AssetManagementSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStats {

    private long totalAssets;
    private long availableAssets;
    private long loanedAssets;
    private long pendingLoans;
    private long overdueLoans;
}
