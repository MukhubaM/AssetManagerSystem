package com.assetmanager.AssetManagementSystem.service.impl;

import com.assetmanager.AssetManagementSystem.entity.AuditLog;
import com.assetmanager.AssetManagementSystem.repository.AuditLogRepository;
import com.assetmanager.AssetManagementSystem.service.AuditLogService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository repository;

    @Override
    @Transactional
    public void log(Long userId, String entityType, Long entityId, String action) {

        AuditLog log = AuditLog.builder()
                .userId(userId)
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .timestamp(LocalDateTime.now())
                .build();

        repository.save(log);
    }
}
