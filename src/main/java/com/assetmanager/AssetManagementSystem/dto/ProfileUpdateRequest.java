package com.assetmanager.AssetManagementSystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Lets any authenticated user (ADMIN, MANAGER, or BORROWER) edit their own
 * name/department, and optionally change their password. Email and role are
 * intentionally not editable here: email is the login identifier, and role
 * changes are an admin-only action (see {@code AdminController}).
 */
@Data
public class ProfileUpdateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String department;

    /**
     * Required only if newPassword is supplied - verified against the
     * current password hash before any change is applied.
     */
    private String currentPassword;

    /**
     * Optional. Leave blank to keep the existing password.
     */
    @Size(min = 0, max = 100)
    private String newPassword;
}