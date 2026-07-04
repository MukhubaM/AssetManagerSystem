package com.assetmanager.AssetManagementSystem.service;

import com.assetmanager.AssetManagementSystem.dto.ProfileUpdateRequest;
import com.assetmanager.AssetManagementSystem.dto.RegistrationRequest;
import com.assetmanager.AssetManagementSystem.entity.Role;
import com.assetmanager.AssetManagementSystem.entity.User;

import java.util.List;

public interface UserService {

    // Public self-registration. Accepts BORROWER or MANAGER (never ADMIN)
    User registerUser(RegistrationRequest request);

    // Used only by the startup bootstrap seeder to create the very first ADMIN account
    User createUser(String name, String department, String email, String rawPassword, Role role);

    List<User> getAllUsers();

    // MANAGER/ADMIN accounts offered as 'Supervisor' choices
    List<User> getPotentialSupervisors();

    User changeRole(Long userId, Role newRole, String actingAdminEmail);

    User setEnabled(Long userId, boolean enabled, String actingAdminEmail);

    User updateProfile(String email, ProfileUpdateRequest request);

    User updateNotificationPreference(String email, boolean emailNotificationsEnabled);

    // Self-service account deletion, available to BORROWER/MANAGER only
    void deleteAccount(String email, String currentPassword);
}