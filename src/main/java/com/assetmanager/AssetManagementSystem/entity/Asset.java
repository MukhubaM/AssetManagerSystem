package com.assetmanager.AssetManagementSystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "assets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assetId;

    @Column(length = 100)
    @NotBlank
    private String title;

    @Column(length = 100)
    @NotBlank
    private String category;

    @Column(unique = true)
    private String serialNumber;

    private LocalDate acquisitionDate;

    private BigDecimal cost;

    // This the rental rate, managers must set when adding an item (startup cost)
    private BigDecimal dailyRate;

    @Column(length = 100)
    @NotBlank
    private String location;

    @Column(length = 100)
    @NotBlank
    private String condition;

    private String photoPath;

    @Enumerated(EnumType.STRING)
    private AssetStatus status;
}