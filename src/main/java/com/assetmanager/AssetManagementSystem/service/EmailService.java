package com.assetmanager.AssetManagementSystem.service;

public interface EmailService {

    // Sends a plain text/prompt email
    void send(String to, String subject, String body);
}