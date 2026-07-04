package com.assetmanager.AssetManagementSystem.exception;

import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {

        log.warn("Resource not found at {}: {}", request.getRequestURI(), ex.getMessage());

        return buildErrorView(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ModelAndView handleBusinessRule(BusinessRuleException ex, HttpServletRequest request) {

        log.warn("Business rule violation at {}: {}", request.getRequestURI(), ex.getMessage());

        return buildErrorView(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneric(Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception at {}", request.getRequestURI(), ex);

        return buildErrorView("Something went wrong. Please try again.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ModelAndView buildErrorView(String message, HttpStatus status) {

        ModelAndView mv = new ModelAndView("error");
        mv.addObject("message", message);
        mv.setStatus(status);

        return mv;
    }
}
