package com.assetmanager.AssetManagementSystem.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class DeleteAccountRequest {

    @NotBlank(message = "Enter your current password to confirm")
    private String currentPassword;
}