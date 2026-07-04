package com.assetmanager.AssetManagementSystem.dto;

import com.assetmanager.AssetManagementSystem.entity.BorrowerStatus;
import com.assetmanager.AssetManagementSystem.entity.Gender;
import com.assetmanager.AssetManagementSystem.entity.ManagerLevel;
import com.assetmanager.AssetManagementSystem.entity.Role;
import jakarta.validation.constraints.*;
        import lombok.Data;

import java.time.LocalDate;

// Public self-registration form, Role is restricted to BORROWER/MANAGER
@Data
public class RegistrationRequest {

    // Personal information
    @NotBlank
    private String name;

    @NotNull(message = "Please select a gender")
    private Gender gender;

    @NotNull(message = "Please enter your date of birth")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;


    // Contact information
    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Pattern(regexp = "^[0-9+\\-() ]{7,20}$", message = "Enter a valid phone number")
    private String phone;

    @NotBlank
    private String address;


    // Employment information
    @NotBlank(message = "Employee number is required")
    private String employeeNumber;

    @NotBlank(message = "ID number is required")
    private String idNumber;

    @NotBlank
    private String department;

    @NotBlank
    private String position;


    // This is a basic, supporting document (ID Copy)
    private org.springframework.web.multipart.MultipartFile document;


    // Account information
    @Size(min = 6, message = "Password must be at least 6 characters")
    @NotBlank
    private String password;

    @NotBlank
    private String confirmPassword;


    // Role
    @NotNull(message = "Please select a role")
    private Role role;


    // For Borrower
    // Always forced to ACTIVE by the service
    private BorrowerStatus borrowerStatus = BorrowerStatus.ACTIVE;


    // For Manager
    private ManagerLevel managerLevel;

    private Boolean canApproveLoans = Boolean.TRUE;

    private Boolean canRegisterAssets = Boolean.TRUE;

    private Boolean canManageUsers = Boolean.TRUE;

    private Boolean canGenerateReports = Boolean.TRUE;


    // Terms
    @AssertTrue(message = "You must agree to the Terms to register")
    private boolean termsAccepted;

    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordConfirmed() {

        return password != null && password.equals(confirmPassword);
    }
}