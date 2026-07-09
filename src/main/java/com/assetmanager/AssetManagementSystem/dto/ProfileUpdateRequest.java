package com.assetmanager.AssetManagementSystem.dto;

import com.assetmanager.AssetManagementSystem.entity.Gender;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import lombok.Data;

import java.time.LocalDate;

// This lets any authenticated user (ADMIN, MANAGER or BORROWER) edit their own details
@Data
public class ProfileUpdateRequest {

    @NotBlank
    @Column(length = 150)
    private String name;

    @Email
    @NotBlank
    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 250)
    private String address;

    private Gender gender;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private String position;

    // Optional
    private org.springframework.web.multipart.MultipartFile profilePicture;

    // Required only if email or newPassword is being changed
    private String currentPassword;

    // Optional
    @Size(min = 0, max = 100)
    private String newPassword;
}
