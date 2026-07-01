package com.assetmanager.AssetManagementSystem.service;

import com.assetmanager.AssetManagementSystem.dto.RegistrationRequest;
import com.assetmanager.AssetManagementSystem.entity.User;

public interface AuditLogService {

    void log(Long userId, String entityType, Long entityId, String action);

    interface UserService {

        User registerUser(RegistrationRequest request);
    }
}
