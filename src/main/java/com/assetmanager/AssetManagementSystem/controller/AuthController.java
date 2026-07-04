package com.assetmanager.AssetManagementSystem.controller;

import com.assetmanager.AssetManagementSystem.dto.RegistrationRequest;
import com.assetmanager.AssetManagementSystem.entity.Gender;
import com.assetmanager.AssetManagementSystem.entity.ManagerLevel;
import com.assetmanager.AssetManagementSystem.entity.Role;
import com.assetmanager.AssetManagementSystem.service.UserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@Controller
@RequiredArgsConstructor
public class AuthController {

    // A fixed department list for the registration form's dropdown
    private static final List<String> DEPARTMENTS = List.of("IT", "Finance", "Human Resources", "Operations", "Procurement", "Facilities", "Warehouse", "Administration", "User", "Other");

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() {

        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {

        model.addAttribute("registrationRequest", new RegistrationRequest());
        addFormReferenceData(model);

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

            addFormReferenceData(model);

            return "register";
        }

        // Role restriction
        userService.registerUser(request);

        return "redirect:/login?registered";
    }

    private void addFormReferenceData(Model model) {

        model.addAttribute("roles", Role.selfRegistrable());
        model.addAttribute("genders", Gender.values());
        model.addAttribute("departments", DEPARTMENTS);
        model.addAttribute("managerLevels", ManagerLevel.values());
    }
}