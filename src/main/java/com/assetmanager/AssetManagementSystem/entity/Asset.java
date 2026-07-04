package com.assetmanager.AssetManagementSystem.entity;

import jakarta.persistence.*;

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

    private String title;

    private String category;

    @Column(unique = true)
    private String serialNumber;

    private LocalDate acquisitionDate;

    private BigDecimal cost;

    // This the rental rate, managers must set when adding an item (startup cost)
    private BigDecimal dailyRate;

    private String location;

    private String condition;

    private String photoPath;

    @Enumerated(EnumType.STRING)
    private AssetStatus status;
}