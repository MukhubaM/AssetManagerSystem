package com.assetmanager.AssetManagementSystem.service;

import com.assetmanager.AssetManagementSystem.dto.ProfileUpdateRequest;
import com.assetmanager.AssetManagementSystem.dto.RegistrationRequest;
import com.assetmanager.AssetManagementSystem.entity.Role;
import com.assetmanager.AssetManagementSystem.entity.User;

import java.util.List;

public interface UserService {

    // Public self-registration
    User registerUser(RegistrationRequest request);

    // Used only by the startup bootstrap seeder to create the first ADMIN account and to let an existing ADMIN create additional staff accounts directly with a given role
    User createUser(String name, String department, String email, String rawPassword, Role role);

    List<User> getAllUsers();

    User changeRole(Long userId, Role newRole, String actingAdminEmail);

    User setEnabled(Long userId, boolean enabled, String actingAdminEmail);

    User updateProfile(String email, ProfileUpdateRequest request);
}