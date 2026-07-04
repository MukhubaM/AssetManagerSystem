package com.assetmanager.AssetManagementSystem.service;

import com.assetmanager.AssetManagementSystem.entity.Notification;
import com.assetmanager.AssetManagementSystem.entity.NotificationType;
import com.assetmanager.AssetManagementSystem.entity.User;

import java.util.List;

public interface NotificationService {

    // This always creates an in-app notification for the recipient and also sends an email for notification types when there's a delay, noticing it in-app matters for tracing
    void notify(User recipient, NotificationType type, String title, String message);

    List<Notification> getNotificationsForUser(String email);

    long getUnreadCount(String email);

    void markAsRead(Long notificationId, String email);

    void markAllAsRead(String email);
}