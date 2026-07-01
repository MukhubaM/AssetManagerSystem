package com.assetmanager.AssetManagementSystem.controller;

import com.assetmanager.AssetManagementSystem.dto.ProfileUpdateRequest;
import com.assetmanager.AssetManagementSystem.entity.User;
import com.assetmanager.AssetManagementSystem.security.CurrentUserProvider;
import com.assetmanager.AssetManagementSystem.service.UserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public String viewProfile(Model model, Authentication auth) {

        User user = currentUserProvider.getCurrentUser().orElseThrow(() -> new IllegalStateException("No authenticated user"));

        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setName(user.getName());
        request.setDepartment(user.getDepartment());

        model.addAttribute("profileRequest", request);
        model.addAttribute("email", user.getEmail());
        model.addAttribute("role", user.getRole());

        return "profile/edit";
    }

    @PostMapping
    public String updateProfile(
            @Valid
            @ModelAttribute("profileRequest")
            ProfileUpdateRequest request,
            BindingResult result,
            Model model,
            Authentication auth) {

        if (result.hasErrors()) {
            User user = currentUserProvider.getCurrentUser().orElseThrow(() -> new IllegalStateException("No authenticated user"));

            model.addAttribute("email", user.getEmail());
            model.addAttribute("role", user.getRole());

            return "profile/edit";
        }

        userService.updateProfile(auth.getName(), request);

        User user = currentUserProvider.getCurrentUser().orElseThrow(() -> new IllegalStateException("No authenticated user"));

        model.addAttribute("success", true);
        model.addAttribute("email", user.getEmail());
        model.addAttribute("role", user.getRole());

        return "profile/edit";
    }
}