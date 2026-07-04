package com.assetmanager.AssetManagementSystem.controller;

import com.assetmanager.AssetManagementSystem.service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public String list(Model model, Authentication auth) {

        model.addAttribute("notifications", notificationService.getNotificationsForUser(auth.getName()));

        return "notifications/list";
    }

    @PostMapping("/{id}/read")
    public String markAsRead(
            @PathVariable
            Long id,
            Authentication auth) {

        notificationService.markAsRead(id, auth.getName());

        return "redirect:/notifications";
    }

    @PostMapping("/read-all")
    public String markAllAsRead(Authentication auth) {

        notificationService.markAllAsRead(auth.getName());

        return "redirect:/notifications";
    }
}