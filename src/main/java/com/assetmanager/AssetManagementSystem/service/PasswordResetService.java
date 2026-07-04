package com.assetmanager.AssetManagementSystem.service;

public interface PasswordResetService {

    // Completes silently even if the email is registered or not, this will prevent attackers from using the form to discover which emails exist in the system
    void requestReset(String email);


    // Throws BusinessRuleException if the token is missing, expired or already used
    void resetPassword(String token, String newPassword);


    // Recovers username
    void recoverUsername(String employeeNumber, String idNumber);
}