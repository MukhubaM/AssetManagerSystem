package com.assetmanager.AssetManagementSystem.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class LoanRequestForm {

    @NotNull(message = "Please choose how many days you need this for")
    @Min(value = 1, message = "Minimum loan duration is 1 day")
    @Max(value = 365, message = "Maximum loan duration is 365 days")
    private Integer durationDays;
}