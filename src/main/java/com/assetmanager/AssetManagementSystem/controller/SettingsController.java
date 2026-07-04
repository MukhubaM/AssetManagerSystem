package com.assetmanager.AssetManagementSystem.controller;

import com.assetmanager.AssetManagementSystem.dto.DeleteAccountRequest;
import com.assetmanager.AssetManagementSystem.dto.NotificationPreferencesRequest;
import com.assetmanager.AssetManagementSystem.entity.User;
import com.assetmanager.AssetManagementSystem.security.CurrentUserProvider;
import com.assetmanager.AssetManagementSystem.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/settings")
public class SettingsController {

    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public String index(Model model) {

        User user = currentUser();

        NotificationPreferencesRequest preferences = new NotificationPreferencesRequest();
        preferences.setEmailNotificationsEnabled(user.isEmailNotificationsEnabled());

        model.addAttribute("preferences", preferences);
        model.addAttribute("user", user);

        return "settings/index";
    }

    @PostMapping("/notifications")
    public String updateNotificationPreferences(
            @ModelAttribute
            NotificationPreferencesRequest preferences,
            Authentication auth,
            Model model) {

        userService.updateNotificationPreference(auth.getName(), preferences.isEmailNotificationsEnabled());

        model.addAttribute("preferences", preferences);
        model.addAttribute("user", currentUser());
        model.addAttribute("success", true);

        return "settings/index";
    }

    @GetMapping("/delete-account")
    public String deleteAccountForm(Model model) {

        model.addAttribute("deleteAccountRequest", new DeleteAccountRequest());

        return "settings/delete-account";
    }

    @PostMapping("/delete-account")
    public String deleteAccount(
            @Valid @ModelAttribute
            DeleteAccountRequest deleteAccountRequest,
            BindingResult result,
            Authentication auth,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        if (result.hasErrors()) {

            return "settings/delete-account";
        }

        userService.deleteAccount(auth.getName(), deleteAccountRequest.getCurrentPassword());

        new SecurityContextLogoutHandler().logout(httpRequest, httpResponse, auth);

        return "redirect:/login?accountDeleted";
    }

    private User currentUser() {

        return currentUserProvider.getCurrentUser().orElseThrow(() -> new IllegalStateException("No authenticated user"));
    }
}