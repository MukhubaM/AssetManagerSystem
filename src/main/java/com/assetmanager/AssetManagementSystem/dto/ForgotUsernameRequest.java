package com.assetmanager.AssetManagementSystem.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;


 //In the login "username" is the account's email address
@Data
public class ForgotUsernameRequest {

    @NotBlank
    @Column(length = 13)
    private String idNumber;

     @NotBlank
     @Column
     private String memberNumber;

}