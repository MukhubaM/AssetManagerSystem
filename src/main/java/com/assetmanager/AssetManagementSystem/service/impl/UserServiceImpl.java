package com.assetmanager.AssetManagementSystem.service.impl;

import com.assetmanager.AssetManagementSystem.dto.RegistrationRequest;
import com.assetmanager.AssetManagementSystem.entity.User;
import com.assetmanager.AssetManagementSystem.exception.BusinessRuleException;
import com.assetmanager.AssetManagementSystem.repository.UserRepository;
import com.assetmanager.AssetManagementSystem.service.AuditLogService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements AuditLogService.UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User registerUser(RegistrationRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessRuleException("Email already registered: " + request.getEmail());        // To only use the email address once
        }

        User user = User.builder()
                .name(request.getName())
                .department(request.getDepartment())
                .email(request.getEmail())
                .role(request.getRole())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        return userRepository.save(user);
    }
}
