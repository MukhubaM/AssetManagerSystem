package com.assetmanager.AssetManagementSystem.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class ForgotPasswordRequest {

    @Email
    @NotBlank
    @Column(unique = true, length = 100)
    private String email;
}