package com.assetmanager.AssetManagementSystem.dto;

import jakarta.validation.constraints.NotBlank;
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

    private String location;

    private String condition;

    private MultipartFile photo;
}
