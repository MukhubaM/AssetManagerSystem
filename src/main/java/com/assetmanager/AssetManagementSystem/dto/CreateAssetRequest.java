package com.assetmanager.AssetManagementSystem.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateAssetRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String category;

    @NotBlank
    private String serialNumber;

    private LocalDate acquisitionDate;

    private BigDecimal cost;

    @NotNull(message = "Daily rate is required")
    @DecimalMin(value = "0.01", message = "Daily rate must be greater than zero")
    private BigDecimal dailyRate;

    @Column(length = 150)
    @NotBlank
    private String location;

    @NotBlank
    @Column(length = 100)
    private String condition;

    private MultipartFile photo;
}