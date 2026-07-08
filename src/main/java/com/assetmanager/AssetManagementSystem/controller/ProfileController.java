package com.assetmanager.AssetManagementSystem.controller;

import com.assetmanager.AssetManagementSystem.dto.ProfileUpdateRequest;
import com.assetmanager.AssetManagementSystem.entity.Gender;
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
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public String viewProfile(Model model) {

        User user = currentUser();

        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setName(user.getName());
        request.setEmail(user.getEmail());
        request.setPhone(user.getPhone());
        request.setAddress(user.getAddress());
        request.setGender(user.getGender());
        request.setDateOfBirth(user.getDateOfBirth());
        request.setPosition(user.getPosition());

        model.addAttribute("profileRequest", request);
        addReadOnlyDetails(model, user);

        return "profile/edit";
    }

    @PostMapping
    public String updateProfile(
            @Valid
            @ModelAttribute("profileRequest")
            ProfileUpdateRequest request,
            BindingResult result,
            Model model,
            Authentication auth,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        if (result.hasErrors()) {
            addReadOnlyDetails(model, currentUser());

            return "profile/edit";
        }

        String originalEmail = auth.getName();
        boolean emailChanging = !request.getEmail().equalsIgnoreCase(originalEmail);
        boolean passwordChanging = request.getNewPassword() != null && !request.getNewPassword().isBlank();

        userService.updateProfile(originalEmail, request);

        if (emailChanging || passwordChanging) {
            new SecurityContextLogoutHandler().logout(httpRequest, httpResponse, auth);                                // The session's authenticated is keyed on the old email/password, forces a fresh login rather than leaving a half-valid session

            return "redirect:/login?" + (emailChanging ? "emailChanged" : "passwordChanged");
        }

        model.addAttribute("success", true);
        addReadOnlyDetails(model, currentUser());

        return "profile/edit";
    }

    private User currentUser() {

        return currentUserProvider.getCurrentUser().orElseThrow(() -> new IllegalStateException("No authenticated user"));
    }

    private void addReadOnlyDetails(Model model, User user) {

        model.addAttribute("email", user.getEmail());
        model.addAttribute("role", user.getRole());
        model.addAttribute("memberNumber", user.getMemberNumber());
        model.addAttribute("idNumber", user.getIdNumber());
        model.addAttribute("department", user.getDepartment());
        model.addAttribute("genders", Gender.values());
        model.addAttribute("profilePicturePath", user.getProfilePicturePath());
    }
}
