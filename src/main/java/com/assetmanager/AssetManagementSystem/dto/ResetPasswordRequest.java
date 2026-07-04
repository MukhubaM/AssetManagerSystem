package com.assetmanager.AssetManagementSystem.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank
    private String token;

    @Size(min = 6, message = "Password must be at least 6 characters")
    @NotBlank
    private String newPassword;

    @NotBlank
    private String confirmPassword;

    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordConfirmed() {

        return newPassword != null && newPassword.equals(confirmPassword);
    }
}