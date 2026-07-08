package com.assetmanager.AssetManagementSystem.config;

import com.assetmanager.AssetManagementSystem.entity.Role;
import com.assetmanager.AssetManagementSystem.repository.UserRepository;
import com.assetmanager.AssetManagementSystem.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// This runs once at startup and creates a single bootstrap admin only if no ADMIN exists(1st user)
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapRunner implements ApplicationRunner {

    private final UserService userService;
    private final UserRepository userRepository;

    @Value("${app.admin.name:System Administrator}")
    private String adminName;

    @Value("${app.admin.department:IT}")
    private String adminDepartment;

    @Value("${app.admin.email:admin@example.com}")
    private String adminEmail;

    @Value("${app.admin.password:ChangeMe123!}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {

        if (userRepository.existsByRole(Role.ADMIN)) {

            return;
        }

        userService.createUser(adminName, adminDepartment, adminEmail, adminPassword, Role.ADMIN);

        //This banner style is for admins to notice immediately in logs that the first admin was generated(only when there's no user created, yet)
        log.warn("=================================================================");
        log.warn(" No ADMIN account existed, created a bootstrap admin:");
        log.warn("   email:    {}", adminEmail);
        log.warn("   password: {}", adminPassword);
        log.warn(" Log in and change this password immediately (Profile page),");
        log.warn(" or set app.admin.email / app.admin.password before first boot.");
        log.warn("=================================================================");
    }
}