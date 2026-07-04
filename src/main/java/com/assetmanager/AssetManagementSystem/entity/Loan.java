package com.assetmanager.AssetManagementSystem.entity;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "borrower_id", nullable = false)
    private User borrower;

    private LocalDate requestDate;

    private LocalDate checkoutDate;

    private LocalDate dueDate;

    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    // Requested loan length in days, chosen by the borrower at request time
    private Integer durationDays;

    // DurationDays * asset.dailyRate at the time of the request, I snap shotted it rather than having it recalculated on the spot
    private BigDecimal totalCost;

    // Set once the 48-hour-before-due warning has been sent
    @Column(nullable = false)
    @ColumnDefault("false")
    @Builder.Default
    private boolean dueSoonNotified = false;

    @OneToOne(mappedBy = "loan", fetch = FetchType.EAGER)
    private Payment payment;
}