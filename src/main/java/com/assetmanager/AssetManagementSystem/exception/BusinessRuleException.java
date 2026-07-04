package com.assetmanager.AssetManagementSystem.exception;

/**
 * {@link GlobalExceptionHandler}   // Handled by GlobalExceptionHandler and rendered as a 409 error
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {

        super(message);
    }
}
