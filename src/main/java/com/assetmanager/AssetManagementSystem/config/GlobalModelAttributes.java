package com.assetmanager.AssetManagementSystem.config;

import com.assetmanager.AssetManagementSystem.service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final NotificationService notificationService;

    @ModelAttribute("unreadNotificationCount")
    public long unreadNotificationCount(Authentication auth) {

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {

            return 0;
        }

        return notificationService.getUnreadCount(auth.getName());
    }
}