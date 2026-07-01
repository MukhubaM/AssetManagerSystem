package com.assetmanager.AssetManagementSystem.dto;

import com.assetmanager.AssetManagementSystem.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class RegistrationRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String department;

    @Email
    @NotBlank
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters")
    @NotBlank
    private String password;

    @NotNull(message = "Please select a role")
    private Role role;
}
