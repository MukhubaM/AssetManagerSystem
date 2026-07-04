package com.assetmanager.AssetManagementSystem.controller;

import com.assetmanager.AssetManagementSystem.dto.ForgotPasswordRequest;
import com.assetmanager.AssetManagementSystem.dto.ForgotUsernameRequest;
import com.assetmanager.AssetManagementSystem.dto.ResetPasswordRequest;
import com.assetmanager.AssetManagementSystem.service.PasswordResetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AccountRecoveryController {

    private final PasswordResetService passwordResetService;

    // Forgot password code scope
    @GetMapping("/forgot-password")
    public String forgotPasswordForm(Model model) {

        model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());

        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPasswordSubmit(
            @Valid @ModelAttribute
            ForgotPasswordRequest forgotPasswordRequest,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "forgot-password";
        }

        passwordResetService.requestReset(forgotPasswordRequest.getEmail());

        model.addAttribute("submitted", true);

        return "forgot-password";
    }


    // Reset password (link from the email)
    @GetMapping("/reset-password")
    public String resetPasswordForm(
            @RequestParam
            String token,
            Model model) {

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(token);

        model.addAttribute("resetPasswordRequest", request);

        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPasswordSubmit(
            @Valid @ModelAttribute
            ResetPasswordRequest resetPasswordRequest,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "reset-password";
        }

        passwordResetService.resetPassword(resetPasswordRequest.getToken(), resetPasswordRequest.getNewPassword());

        return "redirect:/login?passwordReset";
    }

    // Forgot username
    @GetMapping("/forgot-username")
    public String forgotUsernameForm(Model model) {

        model.addAttribute("forgotUsernameRequest", new ForgotUsernameRequest());

        return "forgot-username";
    }

    @PostMapping("/forgot-username")
    public String forgotUsernameSubmit(
            @Valid @ModelAttribute
            ForgotUsernameRequest forgotUsernameRequest,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "forgot-username";
        }

        passwordResetService.recoverUsername(
                forgotUsernameRequest.getEmployeeNumber(),
                forgotUsernameRequest.getIdNumber());

        model.addAttribute("submitted", true);

        return "forgot-username";
    }
}