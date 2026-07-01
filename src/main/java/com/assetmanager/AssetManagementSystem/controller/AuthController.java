package com.assetmanager.AssetManagementSystem.controller;


import com.assetmanager.AssetManagementSystem.dto.RegistrationRequest;
import com.assetmanager.AssetManagementSystem.entity.Role;
import com.assetmanager.AssetManagementSystem.service.AuditLogService;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuditLogService.UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {

        model.addAttribute(
                "registrationRequest",
                new RegistrationRequest());

        model.addAttribute(
                "roles",
                Role.values());

        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid
            @ModelAttribute
            RegistrationRequest request,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {

            model.addAttribute(
                    "roles",
                    Role.values());

            return "register";
        }

        userService.registerUser(request);

        return "redirect:/login";
    }
}