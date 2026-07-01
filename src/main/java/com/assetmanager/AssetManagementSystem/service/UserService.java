package com.assetmanager.AssetManagementSystem.service;

import com.assetmanager.AssetManagementSystem.dto.RegistrationRequest;
import com.assetmanager.AssetManagementSystem.entity.User;

public interface UserService {

    User registerUser(RegistrationRequest request);
}
