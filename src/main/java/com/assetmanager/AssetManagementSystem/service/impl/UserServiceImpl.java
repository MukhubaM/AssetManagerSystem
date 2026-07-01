package com.assetmanager.AssetManagementSystem.service.impl;

import com.assetmanager.AssetManagementSystem.dto.ProfileUpdateRequest;
import com.assetmanager.AssetManagementSystem.dto.RegistrationRequest;
import com.assetmanager.AssetManagementSystem.entity.Role;
import com.assetmanager.AssetManagementSystem.entity.User;
import com.assetmanager.AssetManagementSystem.exception.BusinessRuleException;
import com.assetmanager.AssetManagementSystem.exception.ResourceNotFoundException;
import com.assetmanager.AssetManagementSystem.repository.UserRepository;
import com.assetmanager.AssetManagementSystem.security.CurrentUserProvider;
import com.assetmanager.AssetManagementSystem.service.AuditLogService;
import com.assetmanager.AssetManagementSystem.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public User registerUser(RegistrationRequest request) {

        // This public self-registration only create BORROWER
        // MANAGER/ADMIN accounts are created only via createUser
        return createUser(
                request.getName(),
                request.getDepartment(),
                request.getEmail(),
                request.getPassword(),
                Role.BORROWER
        );
    }

    @Override
    @Transactional
    public User createUser(String name, String department, String email, String rawPassword, Role role) {

        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessRuleException("Email already registered: " + email);
        }

        User user = User.builder()
                .name(name)
                .department(department)
                .email(email)
                .role(role)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        auditLogService.log(currentUserProvider.getCurrentUserId(), "USER", savedUser.getUserId(), "CREATE");

        return savedUser;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByNameAsc();
    }

    @Override
    @Transactional
    public User changeRole(Long userId, Role newRole, String actingAdminEmail) {

        User user = getUser(userId);

        if (user.getEmail().equalsIgnoreCase(actingAdminEmail) && newRole != Role.ADMIN) {
            throw new BusinessRuleException("You cannot remove your own ADMIN role");
        }

        user.setRole(newRole);
        User updatedUser = userRepository.save(user);

        auditLogService.log(currentUserProvider.getCurrentUserId(), "USER", updatedUser.getUserId(),
                "ROLE_CHANGE:" + newRole);

        return updatedUser;
    }

    @Override
    @Transactional
    public User setEnabled(Long userId, boolean enabled, String actingAdminEmail) {

        User user = getUser(userId);

        if (user.getEmail().equalsIgnoreCase(actingAdminEmail) && !enabled) {
            throw new BusinessRuleException("You cannot disable your own account");
        }

        user.setEnabled(enabled);
        User updatedUser = userRepository.save(user);

        auditLogService.log(currentUserProvider.getCurrentUserId(), "USER", updatedUser.getUserId(),
                enabled ? "ENABLE" : "DISABLE");

        return updatedUser;
    }

    @Override
    @Transactional
    public User updateProfile(String email, ProfileUpdateRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        user.setName(request.getName());
        user.setDepartment(request.getDepartment());

        boolean wantsPasswordChange = request.getNewPassword() != null && !request.getNewPassword().isBlank();

        if (wantsPasswordChange) {

            if (request.getCurrentPassword() == null
                    || !passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                throw new BusinessRuleException("Current password is incorrect");
            }

            if (request.getNewPassword().length() < 6) {
                throw new BusinessRuleException("New password must be at least 6 characters");
            }

            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }

        User updatedUser = userRepository.save(user);
        auditLogService.log(currentUserProvider.getCurrentUserId(), "USER", updatedUser.getUserId(), "PROFILE_UPDATE");

        return updatedUser;
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}