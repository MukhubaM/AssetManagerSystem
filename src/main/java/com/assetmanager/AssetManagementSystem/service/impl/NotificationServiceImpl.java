package com.assetmanager.AssetManagementSystem.service.impl;

import com.assetmanager.AssetManagementSystem.entity.Notification;
import com.assetmanager.AssetManagementSystem.entity.NotificationType;
import com.assetmanager.AssetManagementSystem.entity.User;
import com.assetmanager.AssetManagementSystem.exception.ResourceNotFoundException;
import com.assetmanager.AssetManagementSystem.repository.NotificationRepository;
import com.assetmanager.AssetManagementSystem.repository.UserRepository;
import com.assetmanager.AssetManagementSystem.service.EmailService;
import com.assetmanager.AssetManagementSystem.service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Set<NotificationType> EMAIL_ALSO_TYPES = EnumSet.of(NotificationType.LOAN_OVERDUE, NotificationType.LOAN_DUE_SOON, NotificationType.ACCOUNT);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public void notify(User recipient, NotificationType type, String title, String message) {

        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .message(message)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);

        if (EMAIL_ALSO_TYPES.contains(type) && recipient.isEmailNotificationsEnabled()) {
            emailService.send(recipient.getEmail(), title, message);
        }
    }

    @Override
    public List<Notification> getNotificationsForUser(String email) {

        User user = getUser(email);

        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
    }

    @Override
    public long getUnreadCount(String email) {

        User user = getUser(email);

        return notificationRepository.countByRecipientAndReadFalse(user);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, String email) {

        User user = getUser(email);

        Notification notification = notificationRepository.findById(notificationId)
                .filter(n -> n.getRecipient().getUserId().equals(user.getUserId()))
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String email) {

        User user = getUser(email);

        List<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
        notifications.forEach(n -> n.setRead(true));

        notificationRepository.saveAll(notifications);
    }

    private User getUser(String email) {

        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}