package com.assetmanager.AssetManagementSystem.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;


 //In the login "username" is the account's email address
@Data
public class ForgotUsernameRequest {

    @NotBlank
    private String idNumber;

     @NotBlank
     private String memberNumber;

}