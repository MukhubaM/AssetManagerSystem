package com.assetmanager.AssetManagementSystem.service.impl;

import com.assetmanager.AssetManagementSystem.dto.ProfileUpdateRequest;
import com.assetmanager.AssetManagementSystem.dto.RegistrationRequest;
import com.assetmanager.AssetManagementSystem.entity.*;
import com.assetmanager.AssetManagementSystem.exception.BusinessRuleException;
import com.assetmanager.AssetManagementSystem.exception.ResourceNotFoundException;
import com.assetmanager.AssetManagementSystem.repository.UserRepository;
import com.assetmanager.AssetManagementSystem.security.CurrentUserProvider;
import com.assetmanager.AssetManagementSystem.service.AuditLogService;
import com.assetmanager.AssetManagementSystem.service.FileStorageService;
import com.assetmanager.AssetManagementSystem.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final int MINIMUM_AGE_YEARS = 16;
    private static final String PROFILE_PICTURE_SUBDIRECTORY = "profile-pictures";
    private static final String DOCUMENT_SUBDIRECTORY = "documents";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final CurrentUserProvider currentUserProvider;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public User registerUser(RegistrationRequest request) {

        if (!Role.selfRegistrable().contains(request.getRole())) {
            throw new BusinessRuleException("You cannot self-register with role: " + request.getRole());
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessRuleException("Email already registered: " + request.getEmail());
        }

        if (request.getDateOfBirth() != null && Period.between(request.getDateOfBirth(), LocalDate.now()).getYears() < MINIMUM_AGE_YEARS) {
            throw new BusinessRuleException("You must be at least " + MINIMUM_AGE_YEARS + " years old to register");
        }

        String documentPath = fileStorageService.store(request.getDocument(), DOCUMENT_SUBDIRECTORY);

        User adminSupervisor = userRepository.findByRoleAndEnabledTrue(Role.ADMIN)
                .stream()
                .findFirst()
                .orElse(null);

        User.UserBuilder builder = User.builder()
                .name(request.getName())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .idNumber(request.getIdNumber())
                .department(request.getDepartment())
                .position(request.getPosition())
                .role(request.getRole())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .documentPath(documentPath)
                .supervisor(adminSupervisor);

        if (request.getRole() == Role.BORROWER) {

            builder.borrowerStatus(BorrowerStatus.ACTIVE);

        } else if (request.getRole() == Role.MANAGER) {

            builder.managerLevel(request.getManagerLevel() != null ? request.getManagerLevel() : ManagerLevel.ASSET_MANAGER)
                    .canApproveLoans(Boolean.TRUE.equals(request.getCanApproveLoans()))
                    .canRegisterAssets(Boolean.TRUE.equals(request.getCanRegisterAssets()))
                    .canManageUsers(Boolean.TRUE.equals(request.getCanManageUsers()))
                    .canGenerateReports(Boolean.TRUE.equals(request.getCanGenerateReports()));
        }

        User savedUser = userRepository.save(builder.build());
        assignMemberNumber(savedUser);

        auditLogService.log(currentUserProvider.getCurrentUserId(), "USER", savedUser.getUserId(), "REGISTER");

        return savedUser;
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
                .idNumber("N/A")
                .build();

        User savedUser = userRepository.save(user);
        assignMemberNumber(savedUser);

        auditLogService.log(currentUserProvider.getCurrentUserId(), "USER", savedUser.getUserId(), "CREATE");

        return savedUser;
    }

    private void assignMemberNumber(User user) {

        user.setMemberNumber("MEM-" + String.format("%06d", user.getUserId()));
        userRepository.save(user);
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

        auditLogService.log(currentUserProvider.getCurrentUserId(), "USER", updatedUser.getUserId(), "ROLE_CHANGE:" + newRole);

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

        auditLogService.log(currentUserProvider.getCurrentUserId(), "USER", updatedUser.getUserId(), enabled ? "ENABLE" : "DISABLE");

        return updatedUser;
    }

    @Override
    @Transactional
    public User updateProfile(String currentEmail, ProfileUpdateRequest request) {

        User user = userRepository.findByEmail(currentEmail).orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentEmail));

        boolean emailChanging = request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail());
        boolean passwordChanging = request.getNewPassword() != null && !request.getNewPassword().isBlank();

        if (emailChanging || passwordChanging) {
            if (request.getCurrentPassword() == null || !passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                throw new BusinessRuleException("Current password is incorrect");
            }
        }

        if (emailChanging) {

            userRepository.findByEmail(request.getEmail())
                    .filter(existing -> !existing.getUserId().equals(user.getUserId()))
                    .ifPresent(existing -> {
                        throw new BusinessRuleException("Email already in use: " + request.getEmail());
                    });

            user.setEmail(request.getEmail());
        }

        if (passwordChanging) {

            if (request.getNewPassword().length() < 6) {
                throw new BusinessRuleException("New password must be at least 6 characters");
            }

            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }

        if (request.getProfilePicture() != null && !request.getProfilePicture().isEmpty()) {
            user.setProfilePicturePath(fileStorageService.store(request.getProfilePicture(), PROFILE_PICTURE_SUBDIRECTORY));
        }

        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setPosition(request.getPosition());

        User updatedUser = userRepository.save(user);
        auditLogService.log(currentUserProvider.getCurrentUserId(), "USER", updatedUser.getUserId(), "PROFILE_UPDATE");

        return updatedUser;
    }

    @Override
    @Transactional
    public User updateNotificationPreference(String email, boolean emailNotificationsEnabled) {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        user.setEmailNotificationsEnabled(emailNotificationsEnabled);
        User updatedUser = userRepository.save(user);

        auditLogService.log(currentUserProvider.getCurrentUserId(), "USER", updatedUser.getUserId(), "NOTIFICATION_PREFS_UPDATE");

        return updatedUser;
    }

    @Override
    @Transactional
    public void deleteAccount(String email, String currentPassword) {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        if (user.getRole() == Role.ADMIN) {
            throw new BusinessRuleException("Admin accounts can't be self-deleted - ask another admin to deactivate this account");
        }

        if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessRuleException("Current password is incorrect");
        }

        Long userId = user.getUserId();

        // Anonymize rather than hard-delete, the account may be referenced by loan history, audit log entries, notifications and password reset tokens, A literal DELETE will violate a foreign key constraint(Loan.borrower is NOT NULL)
        user.setName("Deleted User");
        user.setEmail("deleted-user-" + userId + "@assetmanager.local");
        user.setPhone(null);
        user.setAddress(null);
        user.setProfilePicturePath(null);
        user.setDocumentPath(null);
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setEnabled(false);

        userRepository.save(user);

        auditLogService.log(userId, "USER", userId, "DELETE_ACCOUNT");
    }

    private User getUser(Long id) {

        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
