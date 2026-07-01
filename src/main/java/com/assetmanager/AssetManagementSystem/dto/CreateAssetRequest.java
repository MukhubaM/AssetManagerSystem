package com.assetmanager.AssetManagementSystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateAssetRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Category must be stated")
    private String category;

    @NotBlank(message = "Serial number is required")
    private String serialNumber;

    @NotBlank
    private LocalDate acquisitionDate;

    @NotBlank
    private BigDecimal cost;

    @NotBlank
    private String location;

    @NotBlank
    private String condition;

    private MultipartFile photo;
}
