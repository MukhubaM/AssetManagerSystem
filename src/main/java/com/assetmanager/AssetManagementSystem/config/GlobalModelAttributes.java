package com.assetmanager.AssetManagementSystem.config;

import com.assetmanager.AssetManagementSystem.entity.User;
import com.assetmanager.AssetManagementSystem.security.CurrentUserProvider;
import com.assetmanager.AssetManagementSystem.service.NotificationService;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final NotificationService notificationService;
    private final CurrentUserProvider currentUserProvider;

    @ModelAttribute("unreadNotificationCount")
    public long unreadNotificationCount(Authentication auth) {

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {

            return 0;
        }

        return notificationService.getUnreadCount(auth.getName());
    }

    // Allows the shared sidebar fragment (rendered on every authenticated page) to show the current user's name/role without every controller needing to add it to its own model
    @ModelAttribute("currentUser")
    public User currentUser(Authentication auth) {

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {

            return null;
        }

        return currentUserProvider.getCurrentUser().orElse(null);
    }

    @ModelAttribute("currentUri")
    public String currentUri(HttpServletRequest request) {

        return request.getRequestURI();
    }
}
