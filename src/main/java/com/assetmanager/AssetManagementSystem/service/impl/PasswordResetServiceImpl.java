package com.assetmanager.AssetManagementSystem.service.impl;

import com.assetmanager.AssetManagementSystem.entity.NotificationType;
import com.assetmanager.AssetManagementSystem.entity.PasswordResetToken;
import com.assetmanager.AssetManagementSystem.entity.User;
import com.assetmanager.AssetManagementSystem.exception.BusinessRuleException;
import com.assetmanager.AssetManagementSystem.repository.PasswordResetTokenRepository;
import com.assetmanager.AssetManagementSystem.repository.UserRepository;
import com.assetmanager.AssetManagementSystem.service.AuditLogService;
import com.assetmanager.AssetManagementSystem.service.EmailService;
import com.assetmanager.AssetManagementSystem.service.NotificationService;
import com.assetmanager.AssetManagementSystem.service.PasswordResetService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final int TOKEN_VALID_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    @Transactional
    public void requestReset(String email) {

        userRepository.findByEmail(email).ifPresent(user -> {

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(UUID.randomUUID().toString())
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusMinutes(TOKEN_VALID_MINUTES))
                    .used(false)
                    .build();

            tokenRepository.save(resetToken);

            String resetLink = baseUrl + "/reset-password?token=" + resetToken.getToken();

            String body = "Hello " + user.getName() + ",\n\n"
                    + "We received a request to reset your Asset Manager password. "
                    + "This link is valid for " + TOKEN_VALID_MINUTES + " minutes:\n\n"
                    + resetLink + "\n\n"
                    + "If you didn't request this, you can safely ignore this email.";

            emailService.send(user.getEmail(), "Reset your Asset Manager password", body);

            auditLogService.log(user.getUserId(), "USER", user.getUserId(), "PASSWORD_RESET_REQUESTED");
        });
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {

        PasswordResetToken resetToken = tokenRepository.findByToken(token).orElseThrow(() -> new BusinessRuleException("This password reset link is invalid."));

        if (resetToken.isUsed()) {
            throw new BusinessRuleException("This password reset link has already been used.");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("This password reset link has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        auditLogService.log(user.getUserId(), "USER", user.getUserId(), "PASSWORD_RESET_COMPLETED");

        notificationService.notify(
                user,
                NotificationType.ACCOUNT, "Your password was changed", "Your Asset Manager password was just reset. If this wasn't you, contact an administrator immediately.");

        log.info("Password reset completed for user {}", user.getEmail());
    }

    @Override
    public void recoverUsername(String memberNumber, String idNumber) {

        userRepository.findByMemberNumberAndIdNumber(memberNumber, idNumber).ifPresent(user -> {

            String body = "Hello " + user.getName() + ",\n\n"
                    + "You (or someone with your member number and ID number) requested a reminder "
                    + "of your Asset Manager username.\n\n"
                    + "Your username is your email address: " + user.getEmail() + "\n\n"
                    + "If you didn't request this, you can safely ignore this email.";

            emailService.send(user.getEmail(), "Your Asset Manager username", body);

            auditLogService.log(user.getUserId(), "USER", user.getUserId(), "USERNAME_RECOVERY_REQUESTED");
        });
    }
}
