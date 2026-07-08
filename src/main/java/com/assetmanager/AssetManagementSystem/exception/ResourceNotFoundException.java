package com.assetmanager.AssetManagementSystem.exception;

/**
 * {@link GlobalExceptionHandler}   Handled by GlobalExceptionHandler and rendered as an error 404
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {

        super(message);
    }
}
